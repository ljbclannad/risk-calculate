package com.example.drools.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.kie.api.KieBase;
import org.kie.api.runtime.StatelessKieSession;

import java.beans.Transient;
import java.util.List;

import static com.example.drools.service.NewKieService.ruleKieBase;

@Data
@TableName("promote_rule")
public class PromoteExecute {
    //促销编号：
    private String promoteCode;
    //业务Kbase
    private transient KieBase workKbase;
    //业务session
    private transient StatelessKieSession workSession;
    //规则内容
    @TableField("promote_rule")
    private String WorkContent;
    //促销规则名称：
    private transient List<String> ruleName;
    private String promoteName;


     public KieBase getWorkKbase() {
        if (this.workKbase == null) {
            this.setWorkKbase();
        }
        return workKbase;
    }

    public void setWorkKbase() {
        this.workKbase = ruleKieBase(this.getWorkContent());
    }

    public StatelessKieSession getWorkSession() {
        if (this.workSession == null) {
            this.setWorkSession();
        }
        return workSession;
    }

    public void setWorkSession() {
        if (null != this.getWorkKbase()) {
            this.workSession = this.getWorkKbase().newStatelessKieSession();
        }
    }
}
