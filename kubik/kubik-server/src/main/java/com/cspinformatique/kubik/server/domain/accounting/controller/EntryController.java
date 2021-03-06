package com.cspinformatique.kubik.server.domain.accounting.controller;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cspinformatique.kubik.server.domain.accounting.model.AccountsExports;
import com.cspinformatique.kubik.server.domain.accounting.model.EntriesExport;
import com.cspinformatique.kubik.server.domain.accounting.service.EntryService;

@Controller
@RequestMapping("/accounting/entry")
public class EntryController {
	@Autowired
	private EntryService entryService;

	@RequestMapping()
	public String getEntriesExportPage() {
		return "accounting/export";
	}

	@RequestMapping(value = "/accounts", produces = "text/csv; charset=utf-8")
	public @ResponseBody AccountsExports exportAccountsEntries(@RequestParam String separator) {
		return new AccountsExports(entryService.generateAccounts(), "COMPTES", separator);
	}

	@RequestMapping(value = "/sales", produces = "text/csv; charset=utf-8")
	public @ResponseBody EntriesExport exportSalesEntries(
			@RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") Date startDate,
			@RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") Date endDate, @RequestParam String separator,
			@RequestParam String decimalSeparator) {

		return new EntriesExport(startDate, endDate,
				entryService.generateSaleJournalEntries(startDate, endDate),
				"VENTES", separator, getDecimalFormat(decimalSeparator));
	}

	@RequestMapping(value = "/treasury", produces = "text/csv; charset=utf-8")
	public @ResponseBody EntriesExport exportTreasuryEntries(
			@RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") Date startDate,
			@RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") Date endDate, @RequestParam String separator,
			@RequestParam String decimalSeparator) {
		return new EntriesExport(startDate, endDate,
				entryService.generateTreasuryJournalEntries(startDate, endDate),
				"TRESORERIE", separator, getDecimalFormat(decimalSeparator));
	}

	private DecimalFormat getDecimalFormat(String decimalSeparator) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(decimalSeparator.toCharArray()[0]);
		
		DecimalFormat decimalFormat = new DecimalFormat("0.00", symbols);
		decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
		
		return decimalFormat;
	}
}
