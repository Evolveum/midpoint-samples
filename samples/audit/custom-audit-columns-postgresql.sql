ALTER TABLE m_audit_event ADD custFoo VARCHAR(255);
ALTER TABLE m_audit_event ADD custBar VARCHAR(48);

CREATE INDEX iAuditEventCustFoo
  ON m_audit_event (custFoo);
CREATE INDEX iAuditEventCustBar
  ON m_audit_event (custBar);

commit;
