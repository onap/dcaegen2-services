export class Topic {
  name: string;
  login: string;
  pass: string;
  dbs: any;
  enable: boolean;
  save_raw: boolean;
  data_format: string;
  ttl: number;
  correlate_cleared_message: boolean;
  message_id_path: string;
  // for UI display
  type: string;
}
