INSERT INTO payment_gateway
VALUES (1, 'VandarPaymentService', true)
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('payment_gateway', 'id'), (SELECT MAX(id) FROM payment_gateway));
