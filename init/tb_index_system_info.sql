/*
 Navicat Premium Data Transfer

 Source Server         : jpa
 Source Server Type    : PostgreSQL
 Source Server Version : 120003
 Source Host           : 192.168.9.64:32189
 Source Catalog        : simulatetest
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 120003
 File Encoding         : 65001

 Date: 26/08/2024 10:23:15
*/

-- ----------------------------
-- Create SEQUENCE
-- ----------------------------
DROP SEQUENCE  IF EXISTS tb_index_system_info_id_seq CASCADE;
CREATE SEQUENCE tb_index_system_info_id_seq INCREMENT BY 1 START WITH 13 MAXVALUE 99999999;
-- ----------------------------
-- Table structure for tb_index_system_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tb_index_system_info";
CREATE TABLE "public"."tb_index_system_info" (
  "id" int4 NOT NULL DEFAULT nextval('tb_index_system_info_id_seq'::regclass),
  "create_time" timestamp(6),
  "creater" varchar(255) COLLATE "pg_catalog"."default",
  "delete" bool,
  "first_index" varchar(255) COLLATE "pg_catalog"."default",
  "four_index" varchar(255) COLLATE "pg_catalog"."default",
  "index_system_name" varchar(255) COLLATE "pg_catalog"."default",
  "model_name" varchar(255) COLLATE "pg_catalog"."default",
  "modify_time" timestamp(6),
  "second_index" varchar(255) COLLATE "pg_catalog"."default",
  "three_index" varchar(255) COLLATE "pg_catalog"."default",
  "source" bool,
  "model_id" int4,
  "batchNo" int4,
  "version" varchar(255) COLLATE "pg_catalog"."default",
  "describe" varchar(255) COLLATE "pg_catalog"."default"
)
;
-- ----------------------------
-- Records of tb_index_system_info
-- ----------------------------
INSERT INTO "public"."tb_index_system_info" VALUES (1, '2024-08-20 07:23:34.328324', '中国电科集团', 'f', 'O', 'F', '中国电科28所业务模型系统', 'gbc', NULL, 'T1', 't1',NULL, 1);
INSERT INTO "public"."tb_index_system_info" VALUES (2, '2024-08-20 07:23:34.328324', 'system', 'f', 'O', 'F', 'ZGDKJT-002', 'gbe', NULL, 'T2', 't1',NULL, 2);
INSERT INTO "public"."tb_index_system_info" VALUES (3, '2024-08-20 07:23:34.328324', 'system', 'f', 'O1', 'F3', 'ZGDKJT-003', 'gb6', NULL, 'T3', 't1',NULL,3);
INSERT INTO "public"."tb_index_system_info" VALUES (4, '2024-08-20 07:23:34.328324', 'system', 'f', 'O3', 'F3', 'ZGDKJT-004', 'gb3', NULL, 'T4', 't1',NULL, 4);
INSERT INTO "public"."tb_index_system_info" VALUES (5, '2024-08-20 07:23:34.328324', 'system', 'f', 'O3', 'F5', 'ZGDKJT-005', 'ghj3', NULL, 'T3','t1', NULL, 5);
INSERT INTO "public"."tb_index_system_info" VALUES (6, '2024-08-20 07:23:34.328324', 'system', 'f', 'O7', 'F5', 'ZGDKJT-006', 'gh58', NULL, 'T9', 't1',NULL, 6);
INSERT INTO "public"."tb_index_system_info" VALUES (7, '2024-08-20 07:23:34.328324', 'system', 'f', 'O7', 'F8', 'ZGDKJT-007', 'gh58', NULL, 'T4', 't1',NULL, 7);
INSERT INTO "public"."tb_index_system_info" VALUES (8, '2024-08-20 07:23:34.328324', 'system', 'f', 'O7', 'F8', 'ZGDKJT-008', 'gh58', NULL, 'T4', 't1',NULL, 8);
INSERT INTO "public"."tb_index_system_info" VALUES (9, '2024-08-20 07:23:34.328324', 'system', 'f', 'O7', 'F8', 'ZGDKJT-009', 'gh58', NULL, 'T4', 't1',NULL, 9);
INSERT INTO "public"."tb_index_system_info" VALUES (10, '2024-08-20 07:23:34.328324', 'system', 'f', 'O7', 'F8', 'ZGDKJT-010', 'gh58', NULL, 'T4', 't1',NULL, 10);
INSERT INTO "public"."tb_index_system_info" VALUES (11, '2024-08-20 07:23:34.328324', 'system', 'f', 'O7', 'F8', 'ZGDKJT-011', 'gh58', NULL, 'T4','t1', NULL, 11);
INSERT INTO "public"."tb_index_system_info" VALUES (12, '2024-08-20 07:23:34.328324', 'system', 'f', 'O7', 'F8', 'ZGDKJT-012', 'gh58', NULL, 'T4', 't1',NULL, 12);

-- ----------------------------
-- Primary Key structure for table tb_index_system_info
-- ----------------------------
ALTER TABLE "public"."tb_index_system_info" ADD CONSTRAINT "tb_index_system_info_pkey" PRIMARY KEY ("id");
