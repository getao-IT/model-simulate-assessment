/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.2.169
 Source Server Type    : PostgreSQL
 Source Server Version : 120003
 Source Host           : 192.168.2.169:32189
 Source Catalog        : simulate_assessment_test
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 120003
 File Encoding         : 65001

 Date: 11/10/2024 14:25:40
*/
-- ----------------------------
-- Create SEQUENCE
-- ----------------------------
DROP SEQUENCE  IF EXISTS tb_system_info_id_seq CASCADE;
CREATE SEQUENCE tb_system_info_id_seq INCREMENT BY 1 START WITH 2 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for tb_system_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tb_system_info";
CREATE TABLE "public"."tb_system_info" (
  "id" int4 NOT NULL DEFAULT nextval('tb_system_info_id_seq'::regclass),
  "delete" bool DEFAULT false,
  "describe" text COLLATE "pg_catalog"."default",
  "import_time" timestamp(6),
  "model_total" int4 DEFAULT 0,
  "status" bool DEFAULT true,
  "system_ip" varchar(255) COLLATE "pg_catalog"."default",
  "system_name" varchar(255) COLLATE "pg_catalog"."default",
  "system_sign" varchar(255) COLLATE "pg_catalog"."default",
  "unit" varchar(255) COLLATE "pg_catalog"."default",
  "user_level" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of tb_system_info
-- ----------------------------
INSERT INTO "public"."tb_system_info" VALUES (1, 'f', NULL, '1900-01-01 00:00:00.000', 0, 't', '192.168.9.64', '中国电科28所业务模型系统', 'ZGDZKJJT28S', '中国电子科技集团28所', '军委');

-- ----------------------------
-- Primary Key structure for table tb_system_info
-- ----------------------------
ALTER TABLE "public"."tb_system_info" ADD CONSTRAINT "tb_system_info_pkey" PRIMARY KEY ("id");
