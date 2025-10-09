 INSERT INTO roles (name) VALUES ('CUSTOMER') ON CONFLICT(name) DO NOTHING;
 INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT(name) DO NOTHING;

INSERT INTO users (id, email, password, salt) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'john.doe@acme.com', 'change_me','salt_val');

INSERT INTO user_roles(user_id, name) VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'CUSTOMER');


INSERT INTO users (id, email, password, salt) VALUES
    ('3e6b2928-413b-4792-9fa3-3c62c0de127e', 'joe_admin@acme.com', 'change_me','salt_val');

INSERT INTO user_roles(user_id, name) VALUES ('3e6b2928-413b-4792-9fa3-3c62c0de127e', 'ADMIN');