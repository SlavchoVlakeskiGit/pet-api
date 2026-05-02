-- Speeds up the species and ownerName filters in GET /v1/pets
CREATE INDEX idx_pets_species ON pets (species);
CREATE INDEX idx_pets_owner_name ON pets (owner_name);

-- Speeds up deleteByUsername in RefreshTokenService
CREATE INDEX idx_refresh_tokens_username ON refresh_tokens (username);
