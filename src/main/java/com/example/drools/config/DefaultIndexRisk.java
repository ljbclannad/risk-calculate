package com.example.drools.config;

import com.example.drools.dao.IndexRiskDAO;
import com.example.drools.dao.IndexRiskFormulaDAO;
import com.example.drools.dao.IndexRiskGroupDAO;
import com.example.drools.dto.IndexRisk;
import com.example.drools.dto.IndexRiskFormula;
import com.example.drools.dto.IndexRiskGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DefaultIndexRisk {

    @Autowired
    IndexRiskDAO indexRiskDAO;

    @Autowired
    IndexRiskGroupDAO indexRiskGroupDAO;

    @Autowired
    IndexRiskFormulaDAO indexRiskFormulaDAO;

    public List<IndexRisk> indexRiskList = new ArrayList<>();

    public List<IndexRiskGroup> indexRiskGroupList = new ArrayList<>();

    public List<IndexRiskFormula> indexRiskFormulaList = new ArrayList<>();

    @PostConstruct
    public void init() {
        indexRiskList = indexRiskDAO.selectList(null);
        indexRiskGroupList = indexRiskGroupDAO.selectList(null);
        indexRiskFormulaList = indexRiskFormulaDAO.selectList(null);
    }
}
