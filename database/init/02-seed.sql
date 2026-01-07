INSERT INTO users (username, password_hash, role) VALUES 
('admin', '$2a$12$sb8KdFEhsVMJBRF7xHZXaeOc92Fg6s0jm7qtgs5lcAAeQqm5QgFo6', 'ADMIN'),
('client', '$2a$12$fjAqk415iEo7bc4QpnHLquKGoGBuhWRlo7kPnFNbF.pth4qtsHupu', 'CLIENT')
ON CONFLICT (username) DO NOTHING;

INSERT INTO vehicles (plate_number, status, latitude, longitude) VALUES 
('B-676-TST', 'AVAILABLE', 44.4268, 26.1025),
('BV-01-ERU', 'BUSY', 45.6427, 25.5887),
('CJ-50-CLJ', 'AVAILABLE', 46.7712, 23.6236)
ON CONFLICT (plate_number) DO NOTHING;

INSERT INTO orders (user_id, pickup_location, destination, status) VALUES
(2, 'Piata Unirii', 'Aeroport Otopeni', 'PENDING');