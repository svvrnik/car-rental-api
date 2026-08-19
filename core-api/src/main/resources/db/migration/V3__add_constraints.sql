ALTER TABLE cars ADD CONSTRAINT chk_cars_price_per_day_positive CHECK ( price_per_day > 0 );
ALTER TABLE rentals ADD CONSTRAINT chk_rentals_dates CHECK ( start_date < end_date );
ALTER TABLE users ADD CONSTRAINT chk_user_balance CHECK ( account_balance >= 0 );
ALTER TABLE transactions ADD CONSTRAINT chk_transactions_amount_positive CHECK ( amount > 0 );
ALTER TABLE rentals ADD CONSTRAINT chk_rentals_cost_positive CHECK ( total_cost >= 0 );
ALTER TABLE transactions ADD CONSTRAINT chk_transaction_participants CHECK (
    (type = 'DEPOSIT' AND sender_id IS NULL AND receiver_id IS NOT NULL) OR
    (type = 'PAYOUT' AND sender_id IS NOT NULL AND receiver_id IS NULL) OR
    (type = 'RENTAL_PAYMENT' AND sender_id IS NOT NULL AND receiver_id IS NOT NULL)
    );