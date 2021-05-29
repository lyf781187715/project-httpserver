package com.lyf.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class TranslateResp implements Serializable {
    String log_id;
    int status;
    int model_type;
    int trans_act;
    String src;
    String trans_res;

    public TranslateResp(String log_id, int status, int model_type, int trans_act, String src, String trans_res) {
        this.log_id = log_id;
        this.status = status;
        this.model_type = model_type;
        this.trans_act = trans_act;
        this.src = src;
        this.trans_res = trans_res;
    }
}

