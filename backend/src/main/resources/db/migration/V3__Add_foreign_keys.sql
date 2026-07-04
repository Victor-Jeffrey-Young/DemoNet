-- Clean up orphan rows before adding constraints
DELETE FROM reviews WHERE item_id NOT IN (SELECT id FROM items);
DELETE FROM reviews WHERE user_id NOT IN (SELECT id FROM users);
DELETE FROM user_items WHERE item_id NOT IN (SELECT id FROM items);
DELETE FROM user_items WHERE user_id NOT IN (SELECT id FROM users);
DELETE FROM item_tag_mapping WHERE item_id NOT IN (SELECT id FROM items);
DELETE FROM item_tag_mapping WHERE tag_id NOT IN (SELECT id FROM tags);

-- Add foreign key constraints with ON DELETE CASCADE
ALTER TABLE reviews
ADD CONSTRAINT fk_reviews_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE;

ALTER TABLE reviews
ADD CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_items
ADD CONSTRAINT fk_user_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_items
ADD CONSTRAINT fk_user_items_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE;

ALTER TABLE item_tag_mapping
ADD CONSTRAINT fk_itm_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE;

ALTER TABLE item_tag_mapping
ADD CONSTRAINT fk_itm_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE;
