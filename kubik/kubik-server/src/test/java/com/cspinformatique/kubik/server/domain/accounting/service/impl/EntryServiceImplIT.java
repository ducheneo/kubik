package com.cspinformatique.kubik.server.domain.accounting.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cspinformatique.kubik.server.KubikServerTest;
import com.cspinformatique.kubik.server.domain.accounting.model.Entry;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KubikServerTest.class)
public class EntryServiceImplIT {
	@Autowired
	EntryServiceImpl entryServiceImpl;

	@Test
	@Transactional
	public void generateSaleJournalEntriesTest() {
		Date startDate = Date.from(LocalDate.of(2015, 2, 01).atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date endDate = Date.from(LocalDate.of(2015, 9, 01).atStartOfDay(ZoneId.systemDefault()).toInstant());

		List<Entry> entries = entryServiceImpl.generateSaleJournalEntries(startDate, endDate);

		HashMap<String, List<Entry>> invoicesEntries = new HashMap<>();

		for (Entry entry : entries) {
			List<Entry> invoiceEntries = invoicesEntries.get(entry.getInvoiceNumber());
			if (invoiceEntries == null) {
				invoiceEntries = new ArrayList<>();
				invoicesEntries.put(entry.getInvoiceNumber(), invoiceEntries);
			}

			invoiceEntries.add(entry);
		}

		// Asserts that every invoices balances.
		List<String> unbalancingInvoices = new ArrayList<>();
		for (List<Entry> invoiceEntries : invoicesEntries.values()) {
			String invoiceNumber = invoiceEntries.get(0).getInvoiceNumber();
			BigDecimal credit = new BigDecimal(0d);
			BigDecimal debit = new BigDecimal(0d);

			for (Entry entry : invoiceEntries) {
				credit = credit.add(new BigDecimal(entry.getCredit()));
				debit = debit.add(new BigDecimal(entry.getDebit()));
			}

			double totalCredit = Precision.round(credit.doubleValue(), 2);
			double totalDebit = Precision.round(debit.doubleValue(), 2);
			if (totalCredit != totalDebit) {
				unbalancingInvoices.add(invoiceNumber);
			}
		}

		Assert.assertTrue("Somes invoices does not balance.", unbalancingInvoices.isEmpty());
	}
}
