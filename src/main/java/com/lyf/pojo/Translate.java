package com.lyf.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Translate {
    String log_id;
    int direct;
    int model_type;
    String src;
    String history;
    String extra_info;

    public Translate(String log_id, int direct, int model_type, String src, String history, String extra_info) {
        this.log_id = log_id;
        this.direct = direct;
        this.model_type = model_type;
        this.src = src;
        this.history = history;
        this.extra_info = extra_info;
    }
}
