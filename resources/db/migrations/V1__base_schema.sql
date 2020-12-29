CREATE TABLE `channel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `logo` text CHARACTER SET utf8 COLLATE utf8_bin,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `archived` tinyint DEFAULT '1',
  `rank` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `channel_title_uindex` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin