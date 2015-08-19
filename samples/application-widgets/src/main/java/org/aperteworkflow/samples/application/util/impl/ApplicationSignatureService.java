package org.aperteworkflow.samples.application.util.impl;

import org.aperteworkflow.samples.application.util.CaseSignatureService;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * Created by Dominik Dębowczyk on 2015-08-12.
 */
public class ApplicationSignatureService implements CaseSignatureService {
    @Override
    public String createSignature() {
        String caseRegistrationDate = getCaseRegistrationDate();
        String caseNo = getCaseNo(caseRegistrationDate);
        String caseSignature =  caseNo +  '/' + caseRegistrationDate;

        return caseSignature;
    }

    private String getCaseRegistrationDate() {
        return new SimpleDateFormat("yy").format(new Date());
    }

    private String getCaseNo(String caseRegistrationDate) {
        ProcessToolContext ctx = getThreadProcessToolContext();
        long seqNo = ctx.getNextValue("CASE_NO/" + caseRegistrationDate);
        return new DecimalFormat("0000").format(seqNo);
    }
}
