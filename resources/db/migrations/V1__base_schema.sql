CREATE TABLE `channel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(50),
  `logo` text,
  `created_at` timestamp NULL DEFAULT now(),
  `updated_at` timestamp NULL DEFAULT now(),
  `archived` tinyint DEFAULT '1',
  `rank` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `channel_title_uindex` (`title`)
) ENGINE=InnoDB;

CREATE TABLE `movie` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(45) NOT NULL,
  `year` int DEFAULT NULL,
  `budget` bigint DEFAULT NULL,
  `channel_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `channel_fk_idx` (`channel_id`),
  CONSTRAINT `channel_fk` FOREIGN KEY (`channel_id`) REFERENCES `channel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;