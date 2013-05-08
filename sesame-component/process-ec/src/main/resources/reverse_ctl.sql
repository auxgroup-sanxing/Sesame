DROP TABLE IF EXISTS reverse_ctl
;
CREATE TABLE reverse_ctl (
  reverse_id varchar(36) NOT NULL,
  service_name varchar(64),
  interface_name varchar(64),
  operation_name varchar(64),
  orginal_serial int default 0,
  orginal_time timestamp,
  request_xml text,
  response_xml text,
  step_no int default 0,
  change_time timestamp,
  task_owner varchar(18),
  task_actor varchar(18),
  status_code integer,
  status_text varchar(64),
  
  PRIMARY KEY  (reverse_id)
) COMMENT='冲正控制表'
;