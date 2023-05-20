/*
 Navicat Premium Data Transfer

 Source Server         : MySQL8
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : digital_carbon

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 17/04/2023 08:35:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for relate_approval_company_user
-- ----------------------------
DROP TABLE IF EXISTS `relate_approval_company_user`;
CREATE TABLE `relate_approval_company_user`  (
  `approval_id` int UNSIGNED NOT NULL,
  `company_id` int UNSIGNED NOT NULL,
  `user_id` int UNSIGNED NOT NULL,
  INDEX `rca_aid`(`approval_id` ASC) USING BTREE,
  INDEX `rca_cid`(`company_id` ASC) USING BTREE,
  INDEX `rca_uid`(`user_id` ASC) USING BTREE,
  CONSTRAINT `rca_aid` FOREIGN KEY (`approval_id`) REFERENCES `repo_approval` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `rca_cid` FOREIGN KEY (`company_id`) REFERENCES `repo_company` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `rca_uid` FOREIGN KEY (`user_id`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of relate_approval_company_user
-- ----------------------------

-- ----------------------------
-- Table structure for relate_company_department
-- ----------------------------
DROP TABLE IF EXISTS `relate_company_department`;
CREATE TABLE `relate_company_department`  (
  `company_id` int UNSIGNED NOT NULL,
  `department_id` int UNSIGNED NOT NULL,
  INDEX `rcd_fcid`(`company_id` ASC) USING BTREE,
  INDEX `rcd_fdid`(`department_id` ASC) USING BTREE,
  CONSTRAINT `rcd_fcid` FOREIGN KEY (`company_id`) REFERENCES `repo_company` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `rcd_fdid` FOREIGN KEY (`department_id`) REFERENCES `repo_department` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of relate_company_department
-- ----------------------------

-- ----------------------------
-- Table structure for relate_company_permission
-- ----------------------------
DROP TABLE IF EXISTS `relate_company_permission`;
CREATE TABLE `relate_company_permission`  (
  `company_id` int UNSIGNED NOT NULL,
  `permission_group_id` int UNSIGNED NOT NULL,
  INDEX `rcp_pfid`(`permission_group_id` ASC) USING BTREE,
  INDEX `rcp_cfid`(`company_id` ASC) USING BTREE,
  CONSTRAINT `rcp_cfid` FOREIGN KEY (`company_id`) REFERENCES `repo_company` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `rcp_pfid` FOREIGN KEY (`permission_group_id`) REFERENCES `repo_permission_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of relate_company_permission
-- ----------------------------

-- ----------------------------
-- Table structure for relate_user_company
-- ----------------------------
DROP TABLE IF EXISTS `relate_user_company`;
CREATE TABLE `relate_user_company`  (
  `user_id` int UNSIGNED NOT NULL,
  `company_id` int UNSIGNED NOT NULL,
  INDEX `ruc_ufid`(`user_id` ASC) USING BTREE,
  INDEX `ruc_cfid`(`company_id` ASC) USING BTREE,
  CONSTRAINT `ruc_cfid` FOREIGN KEY (`company_id`) REFERENCES `repo_company` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ruc_ufid` FOREIGN KEY (`user_id`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of relate_user_company
-- ----------------------------

-- ----------------------------
-- Table structure for relate_user_form
-- ----------------------------
DROP TABLE IF EXISTS `relate_user_form`;
CREATE TABLE `relate_user_form`  (
  `form_id` int UNSIGNED NOT NULL,
  `user_id` int UNSIGNED NOT NULL,
  INDEX `rsu_fuid`(`user_id` ASC) USING BTREE,
  INDEX `rsu_fsid`(`form_id` ASC) USING BTREE,
  CONSTRAINT `rsu_fsid` FOREIGN KEY (`form_id`) REFERENCES `repo_service_form` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `rsu_fuid` FOREIGN KEY (`user_id`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of relate_user_form
-- ----------------------------

-- ----------------------------
-- Table structure for relate_user_permission
-- ----------------------------
DROP TABLE IF EXISTS `relate_user_permission`;
CREATE TABLE `relate_user_permission`  (
  `user_id` int UNSIGNED NOT NULL,
  `permission_group_id` int UNSIGNED NOT NULL,
  INDEX `user_fid`(`user_id` ASC) USING BTREE,
  INDEX `permission_group_fid`(`permission_group_id` ASC) USING BTREE,
  CONSTRAINT `permission_group_fid` FOREIGN KEY (`permission_group_id`) REFERENCES `repo_permission_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user_fid` FOREIGN KEY (`user_id`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of relate_user_permission
-- ----------------------------

-- ----------------------------
-- Table structure for repo_application
-- ----------------------------
DROP TABLE IF EXISTS `repo_application`;
CREATE TABLE `repo_application`  (
  `company_id` int UNSIGNED NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `img_id` tinyint(1) UNSIGNED ZEROFILL NOT NULL,
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `app_public_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `com_public_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `com_private_key` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `notify_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `enable` tinyint UNSIGNED NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL,
  `ability_id` tinyint UNSIGNED NULL DEFAULT NULL,
  `binding_product_id` int NULL DEFAULT NULL,
  INDEX `sk_company`(`company_id` ASC) USING BTREE,
  CONSTRAINT `sk_company` FOREIGN KEY (`company_id`) REFERENCES `repo_company` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repo_application
-- ----------------------------

-- ----------------------------
-- Table structure for repo_approval
-- ----------------------------
DROP TABLE IF EXISTS `repo_approval`;
CREATE TABLE `repo_approval`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `emergency_level` tinyint(3) UNSIGNED ZEROFILL NOT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `state` tinyint(3) UNSIGNED ZEROFILL NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repo_approval
-- ----------------------------

-- ----------------------------
-- Table structure for repo_company
-- ----------------------------
DROP TABLE IF EXISTS `repo_company`;
CREATE TABLE `repo_company`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `company_user_num` bigint UNSIGNED NOT NULL,
  `company_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `company_create_time` datetime NOT NULL,
  `company_industry_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9281481 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repo_company
-- ----------------------------

-- ----------------------------
-- Table structure for repo_department
-- ----------------------------
DROP TABLE IF EXISTS `repo_department`;
CREATE TABLE `repo_department`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `department_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `children_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_id` int UNSIGNED NULL DEFAULT NULL,
  `creator_id` int UNSIGNED NOT NULL,
  `manager_id` int UNSIGNED NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repo_department
-- ----------------------------

-- ----------------------------
-- Table structure for repo_message
-- ----------------------------
DROP TABLE IF EXISTS `repo_message`;
CREATE TABLE `repo_message`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `receiver` int UNSIGNED NOT NULL,
  `sender` int UNSIGNED NOT NULL,
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NOT NULL,
  `is_read` tinyint UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `rms_fuid`(`receiver` ASC) USING BTREE,
  INDEX `rms_fsid`(`sender` ASC) USING BTREE,
  CONSTRAINT `rms_fsid` FOREIGN KEY (`sender`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `rms_fuid` FOREIGN KEY (`receiver`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repo_message
-- ----------------------------

-- ----------------------------
-- Table structure for repo_order_record
-- ----------------------------
DROP TABLE IF EXISTS `repo_order_record`;
CREATE TABLE `repo_order_record`  (
  `user_id` int UNSIGNED NOT NULL,
  `order_num` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NOT NULL,
  `biz_state` tinyint UNSIGNED NOT NULL,
  `buy_year` int NOT NULL,
  INDEX `ror_fuid`(`user_id` ASC) USING BTREE,
  CONSTRAINT `ror_fuid` FOREIGN KEY (`user_id`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repo_order_record
-- ----------------------------

-- ----------------------------
-- Table structure for repo_payment
-- ----------------------------
DROP TABLE IF EXISTS `repo_payment`;
CREATE TABLE `repo_payment`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `hold_user_id` int UNSIGNED NOT NULL,
  `ending_date` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `rp_fuid`(`hold_user_id` ASC) USING BTREE,
  CONSTRAINT `rp_fuid` FOREIGN KEY (`hold_user_id`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repo_payment
-- ----------------------------

-- ----------------------------
-- Table structure for repo_permission_group
-- ----------------------------
DROP TABLE IF EXISTS `repo_permission_group`;
CREATE TABLE `repo_permission_group`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `group_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `permissions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `create_user_id` int UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `rpu_fuid`(`create_user_id` ASC) USING BTREE,
  CONSTRAINT `rpu_fuid` FOREIGN KEY (`create_user_id`) REFERENCES `repo_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 817 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repo_permission_group
-- ----------------------------
INSERT INTO `repo_permission_group` VALUES (1, '超级管理员', 'admin.super.*', '系统默认权限组禁止删除', '2023-03-24 00:16:42', 1);

-- ----------------------------
-- Table structure for repo_service_form
-- ----------------------------
DROP TABLE IF EXISTS `repo_service_form`;
CREATE TABLE `repo_service_form`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `industry_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `region_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `company_needs` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `post_timestamp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repo_service_form
-- ----------------------------

-- ----------------------------
-- Table structure for repo_user
-- ----------------------------
DROP TABLE IF EXISTS `repo_user`;
CREATE TABLE `repo_user`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `portrait` int UNSIGNED NOT NULL DEFAULT 0,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `realname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `city` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `enable` tinyint UNSIGNED NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of repo_user
-- ----------------------------
INSERT INTO `repo_user` VALUES (1, 0, 'root', '数碳智能', 'root@carbon.com', '1', '$2a$10$B9c1hNWIuumcWPbAjlL3zeXXaDNuxl36tMXkrN2aZ1vZZwCL8cW3W', '16666666666', NULL, 1, '2023-03-23 22:01:43');

-- ----------------------------
-- Table structure for statistic_2023_04
-- ----------------------------
DROP TABLE IF EXISTS `statistic_2023_04`;
CREATE TABLE `statistic_2023_04`  (
  `company_id` int NOT NULL,
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_id` int NOT NULL,
  `1` double UNSIGNED NOT NULL DEFAULT 0,
  `2` double UNSIGNED NOT NULL DEFAULT 0,
  `3` double UNSIGNED NOT NULL DEFAULT 0,
  `4` double UNSIGNED NOT NULL DEFAULT 0,
  `5` double UNSIGNED NOT NULL DEFAULT 0,
  `6` double UNSIGNED NOT NULL DEFAULT 0,
  `7` double UNSIGNED NOT NULL DEFAULT 0,
  `8` double UNSIGNED NOT NULL DEFAULT 0,
  `9` double UNSIGNED NOT NULL DEFAULT 0,
  `10` double UNSIGNED NOT NULL DEFAULT 0,
  `11` double UNSIGNED NOT NULL DEFAULT 0,
  `12` double UNSIGNED NOT NULL DEFAULT 0,
  `13` double UNSIGNED NOT NULL DEFAULT 0,
  `14` double UNSIGNED NOT NULL DEFAULT 0,
  `15` double UNSIGNED NOT NULL DEFAULT 0,
  `16` double UNSIGNED NOT NULL DEFAULT 0,
  `17` double UNSIGNED NOT NULL DEFAULT 0,
  `18` double UNSIGNED NOT NULL DEFAULT 0,
  `19` double UNSIGNED NOT NULL DEFAULT 0,
  `20` double UNSIGNED NOT NULL DEFAULT 0,
  `21` double UNSIGNED NOT NULL DEFAULT 0,
  `22` double UNSIGNED NOT NULL DEFAULT 0,
  `23` double UNSIGNED NOT NULL DEFAULT 0,
  `24` double UNSIGNED NOT NULL DEFAULT 0,
  `25` double UNSIGNED NOT NULL DEFAULT 0,
  `26` double UNSIGNED NOT NULL DEFAULT 0,
  `27` double UNSIGNED NOT NULL DEFAULT 0,
  `28` double UNSIGNED NOT NULL DEFAULT 0,
  `29` double UNSIGNED NOT NULL DEFAULT 0,
  `30` double UNSIGNED NOT NULL DEFAULT 0,
  `31` double UNSIGNED NOT NULL DEFAULT 0
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of statistic_2023_04
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
