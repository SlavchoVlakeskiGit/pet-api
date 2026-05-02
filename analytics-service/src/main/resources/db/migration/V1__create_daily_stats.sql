CREATE TABLE IF NOT EXISTS daily_stats (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    stat_date  DATE         NOT NULL,
    action     VARCHAR(20)  NOT NULL,
    count      BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_daily_stats_date_action (stat_date, action)
);
