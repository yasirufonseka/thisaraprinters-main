-- Seed default material categories (only insert if not already present)
INSERT INTO category (name) SELECT 'Paper' WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Paper');
INSERT INTO category (name) SELECT 'Ink' WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Ink');
INSERT INTO category (name) SELECT 'Plate' WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Plate');
INSERT INTO category (name) SELECT 'Blanket' WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Blanket');
INSERT INTO category (name) SELECT 'Chemical Solution' WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Chemical Solution');
INSERT INTO category (name) SELECT 'Powder Spray' WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Powder Spray');
INSERT INTO category (name) SELECT 'Pieces' WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Pieces');
