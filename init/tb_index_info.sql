/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.9.64
 Source Server Type    : PostgreSQL
 Source Server Version : 120003
 Source Host           : 192.168.9.64:32189
 Source Catalog        : simulate_assessment_test
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 120003
 File Encoding         : 65001

 Date: 20/08/2024 16:10:59
*/


-- ----------------------------
-- Create SEQUENCE
-- ----------------------------
DROP SEQUENCE  IF EXISTS tb_index_info_id_seq CASCADE;
CREATE SEQUENCE tb_index_info_id_seq INCREMENT BY 1 START WITH 25 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for tb_index_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tb_index_info";
CREATE TABLE "public"."tb_index_info"
(
    "id"          int4 NOT NULL DEFAULT nextval('"tb_index_info_id_seq"'::regclass),
    "index_name"  varchar(255) COLLATE "pg_catalog"."default",
    "level"       int4,
    "model_id"    int4,
    "batch_no"    int4,
    "creater"     varchar(255) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "modify_time" timestamp(6),
    "delete"      bool,
    "sign" varchar(255) COLLATE "pg_catalog"."default",
    "parent_index_id" int4
);

-- ----------------------------
-- Records of tb_index_info
-- ----------------------------
INSERT INTO "public"."tb_index_info" VALUES (1, '可用性', 1, NULL, 1, 'system', '2024-08-20 07:23:34.328324', NULL, 'f', 'FHGXFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (2, '数据实时性', 1, NULL, 1, 'system', '2024-08-20 07:55:09.185676', NULL, 'f', 'FHGXFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (3, '接口符合性', 1, NULL, 1, 'system', '2024-08-20 07:55:09.189504', NULL, 'f', 'FHGXFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (4, '格式规范性', 1, NULL, 1, 'system', '2024-08-20 07:55:09.190614', NULL, 'f', 'FHGXFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (5, '府会关系分析能力', 2, NULL, 1, 'system', '2024-08-20 08:00:04.478505', NULL, 'f', 'FHGXFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (6, '体系贡献率', 2, NULL, 1, 'system', '2024-08-20 08:00:04.479843', NULL, 'f', 'FHGXFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (7, '元首政坛丑闻分析能力', 3, NULL, 1, 'system', '2024-08-20 08:00:04.481001', NULL, 'f', 'FHGXFX', 5);
INSERT INTO "public"."tb_index_info" VALUES (8, '议员政策法案分析能力', 3, NULL, 1, 'system', '2024-08-20 08:00:04.481973', NULL, 'f', 'FHGXFX', 5);
INSERT INTO "public"."tb_index_info" VALUES (9, '政要丑闻类型', 4, NULL, 1, 'system', '2024-08-20 08:00:04.482894', NULL, 'f', 'FHGXFX', 7);
INSERT INTO "public"."tb_index_info" VALUES (10, '政要丑闻数量', 4, NULL, 1, 'system', '2024-08-20 08:00:04.483915', NULL, 'f', 'FHGXFX', 7);
INSERT INTO "public"."tb_index_info" VALUES (11, '议员政策法案类型', 4, NULL, 1, 'system', '2024-08-20 08:00:04.48685', NULL, 'f', 'FHGXFX', 8);
INSERT INTO "public"."tb_index_info" VALUES (12, '议员政策法案数量', 4, NULL, 1, 'system', '2024-08-20 08:00:04.489406', NULL, 'f', 'FHGXFX', 8);
INSERT INTO "public"."tb_index_info" VALUES (13, '可用性', 1, NULL, 1, 'system', '2024-08-20 07:23:34.328324', NULL, 'f', 'ZDRWLLFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (14, '数据实时性', 1, NULL, 1, 'system', '2024-08-20 07:55:09.185676', NULL, 'f', 'ZDRWLLFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (15, '接口符合性', 1, NULL, 1, 'system', '2024-08-20 07:55:09.189504', NULL, 'f', 'ZDRWLLFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (16, '格式规范性', 1, NULL, 1, 'system', '2024-08-20 07:55:09.190614', NULL, 'f', 'ZDRWLLFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (17, '府会关系分析能力', 2, NULL, 1, 'system', '2024-08-20 08:00:04.478505', NULL, 'f', 'ZDRWLLFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (18, '体系贡献率', 2, NULL, 1, 'system', '2024-08-20 08:00:04.479843', NULL, 'f', 'ZDRWLLFX', 0);
INSERT INTO "public"."tb_index_info" VALUES (19, '元首政坛丑闻分析能力', 3, NULL, 1, 'system', '2024-08-20 08:00:04.481001', NULL, 'f', 'ZDRWLLFX', 17);
INSERT INTO "public"."tb_index_info" VALUES (20, '议员政策法案分析能力', 3, NULL, 1, 'system', '2024-08-20 08:00:04.481973', NULL, 'f', 'ZDRWLLFX', 17);
INSERT INTO "public"."tb_index_info" VALUES (21, '政要丑闻类型', 4, NULL, 1, 'system', '2024-08-20 08:00:04.482894', NULL, 'f', 'ZDRWLLFX', 19);
INSERT INTO "public"."tb_index_info" VALUES (22, '政要丑闻数量', 4, NULL, 1, 'system', '2024-08-20 08:00:04.483915', NULL, 'f', 'ZDRWLLFX', 19);
INSERT INTO "public"."tb_index_info" VALUES (23, '议员政策法案类型', 4, NULL, 1, 'system', '2024-08-20 08:00:04.48685', NULL, 'f', 'ZDRWLLFX', 20);
INSERT INTO "public"."tb_index_info" VALUES (24, '议员政策法案数量', 4, NULL, 1, 'system', '2024-08-20 08:00:04.489406', NULL, 'f', 'ZDRWLLFX', 20);

-- ----------------------------
-- Primary Key structure for table tb_index_info
-- ----------------------------
ALTER TABLE "public"."tb_index_info" ADD CONSTRAINT "model_index_info_pkey" PRIMARY KEY ("id");

