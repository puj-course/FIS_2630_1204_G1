BEGIN;

ALTER TABLE aseo
    ADD COLUMN IF NOT EXISTS stock_minimo INTEGER NOT NULL DEFAULT 0;

ALTER TABLE maternidad
    ADD COLUMN IF NOT EXISTS stock_minimo INTEGER NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_aseo_stock_minimo'
    ) THEN
        ALTER TABLE aseo
            ADD CONSTRAINT ck_aseo_stock_minimo CHECK (stock_minimo >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_maternidad_stock_minimo'
    ) THEN
        ALTER TABLE maternidad
            ADD CONSTRAINT ck_maternidad_stock_minimo CHECK (stock_minimo >= 0);
    END IF;
END
$$;

COMMIT;
