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

 Date: 26/08/2024 12:42:02
*/
-- ----------------------------
-- Create SEQUENCE
-- ----------------------------
DROP SEQUENCE  IF EXISTS tb_model_info_id_seq CASCADE;
CREATE SEQUENCE tb_model_info_id_seq INCREMENT BY 1 START WITH 13 MAXVALUE 99999999;

-- ----------------------------
-- Table structure for tb_model_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."tb_model_info";
CREATE TABLE "public"."tb_model_info" (
  "id" int4 NOT NULL DEFAULT nextval('tb_model_info_id_seq'::regclass),
  "assessment_count" int4,
  "delete" bool,
  "describe" text COLLATE "pg_catalog"."default",
  "field" varchar(255) COLLATE "pg_catalog"."default",
  "keyword" varchar(255) COLLATE "pg_catalog"."default",
  "model_name" varchar(255) COLLATE "pg_catalog"."default",
  "service_type" varchar(255) COLLATE "pg_catalog"."default",
  "status" bool,
  "system_id" int4,
  "unit" varchar(255) COLLATE "pg_catalog"."default",
  "user_level" varchar(255) COLLATE "pg_catalog"."default",
  "version" varchar(255) COLLATE "pg_catalog"."default",
  "sign" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of tb_model_info
-- ----------------------------
INSERT INTO "public"."tb_model_info" VALUES (1, 0, 'f', '府会关系分析模型，用来分析政党机关从政人员府会关系', '陆,海,空,天', '府会分析、关系分析', '府会关系分析模型'
, '分析研判模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'FHGXFX');
INSERT INTO "public"."tb_model_info" VALUES (2, 0, 'f', '政党人物履历分析模型，用来分析政党机关从业人员的履历关系', '陆,空,天', '履历分析、政党', '政党人物履历分析模型'
, '需求融合模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'ZDRWLLFX');
INSERT INTO "public"."tb_model_info" VALUES (3, 0, 'f', '评论共享模型，用来共享言论或评论信息', '海,空,天', '评论分析、评论共享', '评论共享模型'
, '评论分发共享模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'PLGX');
INSERT INTO "public"."tb_model_info" VALUES (4, 0, 'f', '言论挖掘模型，用来挖掘国会政党言论信息', '陆,天', '言论发掘、国会政党', '言论挖掘模型'
, '言论分析评判模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'YLWJ');
INSERT INTO "public"."tb_model_info" VALUES (5, 0, 'f', '战略趋势发展分析模型，用来根据实时形势分析预测战事发展趋势', '陆,海,空,天', '战略分析、战争发展趋势', '战略趋势发展分析模型'
, '战略战事研判模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'ZLQSFZFX');
INSERT INTO "public"."tb_model_info" VALUES (6, 0, 'f', '政党人物履历分析模型，用来分析政党机关从业人员的履历关系', '陆,空,天', '履历分析、政党', '政党人物履历分析模型-1'
, '需求融合模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'ZDRWLLFX1');
INSERT INTO "public"."tb_model_info" VALUES (7, 0, 'f', '评论共享模型，用来共享言论或评论信息', '海,空,天', '评论分析、评论共享', '评论共享模型-1'
, '评论分发共享模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'PLGX1');
INSERT INTO "public"."tb_model_info" VALUES (8, 0, 'f', '言论挖掘模型，用来挖掘国会政党言论信息', '陆,天', '言论发掘、国会政党', '言论挖掘模型-1'
, '言论分析评判模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'YLWJ1');
INSERT INTO "public"."tb_model_info" VALUES (9, 0, 'f', '战略趋势发展分析模型，用来根据实时形势分析预测战事发展趋势', '陆,海,空,天', '战略分析、战争发展趋势', '战略趋势发展分析模型-1'
, '战略战事研判模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'ZLQSFZFX1');
INSERT INTO "public"."tb_model_info" VALUES (10, 0, 'f', '政党人物履历分析模型，用来分析政党机关从业人员的履历关系', '陆,空,天', '履历分析、政党', '政党人物履历分析模型-2'
, '需求融合模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'ZDRWLLFX2');
INSERT INTO "public"."tb_model_info" VALUES (11, 0, 'f', '评论共享模型，用来共享言论或评论信息', '海,空,天', '评论分析、评论共享', '评论共享模型-3'
, '评论分发共享模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'PLGX3');
INSERT INTO "public"."tb_model_info" VALUES (12, 0, 'f', '言论挖掘模型，用来挖掘国会政党言论信息', '陆,天', '言论发掘、国会政党', '言论挖掘模型-4'
, '言论分析评判模型', 't', 1, '中国电科28所', 'JBZ', 'v1.0.0', 'YLWJ4');
-- ----------------------------
-- Primary Key structure for table tb_model_info
-- ----------------------------
ALTER TABLE "public"."tb_model_info" ADD CONSTRAINT "tb_model_info_pkey" PRIMARY KEY ("id");
