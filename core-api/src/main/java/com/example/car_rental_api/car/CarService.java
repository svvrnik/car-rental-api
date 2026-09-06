package com.example.car_rental_api.car;

import com.example.car_rental_api.car.dto.CarRequestDto;
import com.example.car_rental_api.car.dto.CarResponseDto;
import com.example.car_rental_api.car.image.CarImage;
import com.example.car_rental_api.car.mapper.CarMapper;
import com.example.car_rental_api.exception.*;
import com.example.car_rental_api.rental.RentalRepository;
import com.example.car_rental_api.rental.RentalStatus;
import com.example.car_rental_api.storage.FileStorageService;
import com.example.car_rental_api.user.Role;
import com.example.car_rental_api.user.User;
import com.example.car_rental_api.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final CarMapper carMapper;
    private final String companyEmail;
    private final FileStorageService fileStorageService;
    private final RentalRepository rentalRepository;
    private final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public CarService(CarRepository carRepository, UserRepository userRepository, CarMapper carMapper, @Value("${app.company.email}") String companyEmail, FileStorageService fileStorageService, RentalRepository rentalRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.carMapper = carMapper;
        this.companyEmail = companyEmail;
        this.fileStorageService = fileStorageService;
        this.rentalRepository = rentalRepository;
    }

    public CarResponseDto addCar(CarRequestDto carRequestDto, MultipartFile[] files){
        String emailOfLoggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User foundUser = userRepository.findByEmail(emailOfLoggedInUser).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(carRepository.existsByLicensePlate(carRequestDto.getLicensePlate())){
            throw new DuplicateLicensePlateException("Car with this license plate exists.");
        }
        Car createdCar = carMapper.carRequestDtoToCar(carRequestDto);

        if(foundUser.getRole() == Role.ADMIN){
            createdCar.setStatus(CarStatus.AVAILABLE);
            User companyAccount = userRepository.findByEmail(companyEmail).orElseThrow(() -> new CompanyAccountNotFoundException("Company account is missing in the database")); //only for educational purpose - when renting car, everything goes to company account (virtual wallet)
            companyAccount.addCar(createdCar);
        }else{
            createdCar.setStatus(CarStatus.PENDING);
            foundUser.addCar(createdCar);
        }

        if(files != null && files.length > 10) {
            throw new InvalidFileException("You can upload a maximum of 10 images");
        }

        List<String> uploadedFilesNames = new ArrayList<>();
        try{
            if(files!=null && files.length!=0){
                for(int i=0; i<files.length; i++){
                    MultipartFile file = files[i];

                    if(file.isEmpty()){
                        throw new InvalidFileException("File cannot be empty.");
                    }

                    if(file.getContentType()==null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType()) || !fileStorageService.hasValidExtension(file.getOriginalFilename())){
                        throw new InvalidFileException("Only JPEG, PNG and WEBP files are allowed.");
                    }

                    if(file.getSize()>MAX_FILE_SIZE){
                        throw new InvalidFileException("Each file must be smaller than 10MB");
                    }

                    String imageName = fileStorageService.uploadFile(file);
                    String imageUrl = fileStorageService.createUrlForFile(imageName);

                    CarImage carImage = new CarImage();
                    carImage.setCar(createdCar);
                    carImage.setImageUrl(imageUrl);
                    carImage.setMain(i == 0);

                    createdCar.getImages().add(carImage);
                    uploadedFilesNames.add(imageName);
                }
            }

            Car savedCar = carRepository.save(createdCar);

            return carMapper.carToCarResponseDto(savedCar);
        }catch(Exception e){
            try{
                fileStorageService.deleteFiles(uploadedFilesNames);
            }catch(Exception deleteFilesException){
                e.addSuppressed(deleteFilesException);
            }
            throw e;
        }
    }

    public Page<CarResponseDto> getAvailableCars(Pageable pageable){
        return carRepository.findByStatus(CarStatus.AVAILABLE, pageable)
                .map(carMapper::carToCarResponseDto);
    }

    public Page<CarResponseDto> getUserCars(Pageable pageable){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return carRepository.findByOwnerEmail(email, pageable).map(carMapper::carToCarResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CarResponseDto approveCar(Long carId){
        Car foundCar = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car not found"));
        foundCar.setStatus(CarStatus.AVAILABLE);
        Car savedCar = carRepository.save(foundCar);
        return carMapper.carToCarResponseDto(savedCar);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CarResponseDto rejectCar(Long carId){
        Car foundCar = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car not found"));
        foundCar.setStatus(CarStatus.REJECTED);
        Car savedCar = carRepository.save(foundCar);
        return carMapper.carToCarResponseDto(savedCar);
    }

    public CarResponseDto withdrawCar(Long carId){
        String emailOfLoggedUser = SecurityContextHolder.getContext().getAuthentication().getName();

        Car foundCar = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car not found"));

        if(!foundCar.getOwner().getEmail().equals(emailOfLoggedUser)){
            throw new UserIsNotCarOwnerException("You are not owner of this car");
        }
        if(rentalRepository.existsByCarIdAndStatus(foundCar.getId(), RentalStatus.ACTIVE)){
            throw new CarCurrentlyRentedException("Car has an active rental");
        }
        foundCar.setStatus(CarStatus.UNAVAILABLE);
        Car savedCar = carRepository.save(foundCar);
        return carMapper.carToCarResponseDto(savedCar);
    }
}
