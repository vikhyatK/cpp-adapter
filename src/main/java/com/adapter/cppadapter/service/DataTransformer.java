package com.adapter.cppadapter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.adapter.cppadapter.model.ExecAllocGrp;
import com.adapter.cppadapter.model.Instrument;
import com.adapter.cppadapter.model.OrdAllocGrp;
import com.adapter.cppadapter.model.PartiesSubGroup;
import com.adapter.cppadapter.model.Party;
import com.adapter.cppadapter.model.Payload;
import com.adapter.cppadapter.model.Quote;
import com.adapter.cppadapter.model.TradeCaptureReport;
import com.adapter.cppadapter.model.UserDefinedTags;
import com.adapter.cppadapter.model.YieldData;
import com.omxgroup.xstream.amp.AmpTradeRep;

@Service
public class DataTransformer {

	public Payload createPayloadFromResponse(AmpTradeRep data) {
		Payload payload = new Payload();
		
		if(data.getTradeId().getTradeDate() !=null)
			payload.setTradeDate(data.getTradeId().getTradeDate().toString());
		else if(data.getDynamics().getAmendTime().getDate() != null)
			payload.setTradeDate(data.getDynamics().getAmendTime().getDate().toString());
		
		ExecAllocGrp execAllocGrp = new ExecAllocGrp();
		execAllocGrp.setTradeId(data.getTradeId().getTradeNo().value);
		payload.setExecAllocGrp(execAllocGrp);
		
		OrdAllocGrp buyOrdAlloc = new OrdAllocGrp();
		buyOrdAlloc.setOrderSide("Buy");
		buyOrdAlloc.setOrderDate(data.getStatics().getBuyOrderId().getOrderDate().toString());
		buyOrdAlloc.setOrderID((int)data.getStatics().getBuyOrderId().getOrderDate().value);
		buyOrdAlloc.setSecondaryClOrdID(data.getStatics().getBuyBrokerRef().toString());
		buyOrdAlloc.setCIOrdID(data.getStatics().getBuyExtOrderId().toString());
		List<Party> buyParties = new ArrayList<>();
		buyParties.add(createParty(new String(data.getStatics().getBuyTraderId().value), 12, null));
		buyParties.add(createParty(new String(data.getStatics().getBuyOperatorId().value), 36, null));
		List<PartiesSubGroup> buyPartiesSubGroups1 = new ArrayList<>();
		buyPartiesSubGroups1.add(createPartySubGroups(data.getStatics().getIsBuyOrderPublic().toString(), 4001));
		buyPartiesSubGroups1.add(createPartySubGroups(data.getStatics().getBuyShortBrokerRef().toString(), 4002));
		buyParties.add(createParty(new String(data.getStatics().getSellFirmId().value), 1, buyPartiesSubGroups1));
		buyParties.add(createParty(new String(data.getStatics().getBuySponsoringFirmId().value), 7, null));
		List<PartiesSubGroup> buyPartiesSubGroups2 = new ArrayList<>();
		buyPartiesSubGroups2.add(createPartySubGroups(new String(data.getStatics().getBuyGiveUpRef().value), 4003));
		buyParties.add(createParty(new String(data.getStatics().getBuyGiveUpFirmId().value), 3, buyPartiesSubGroups2));
		buyParties.add(createParty(new String(data.getDynamics().getBuyAccountChoice().getDepAccId().value), 10, null));
		buyOrdAlloc.setParties(buyParties);
		
		
		
		
		
		OrdAllocGrp sellOrdAlloc = new OrdAllocGrp();
		sellOrdAlloc.setOrderSide("Sell");
		sellOrdAlloc.setOrderDate(data.getStatics().getSellOrderId().getOrderDate().toString());
		sellOrdAlloc.setOrderID((int)data.getStatics().getSellOrderId().getOrderDate().value);
		sellOrdAlloc.setSecondaryClOrdID(data.getStatics().getSellBrokerRef().toString());
		sellOrdAlloc.setCIOrdID(data.getStatics().getSellExtOrderId().toString());
		List<Party> sellParties = new ArrayList<>();
		sellParties.add(createParty(new String(data.getStatics().getSellTraderId().value), 12, null));
		sellParties.add(createParty(new String(data.getStatics().getSellOperatorId().value), 36, null));
		List<PartiesSubGroup> sellPartiesSubGroups1 = new ArrayList<>();
		sellPartiesSubGroups1.add(createPartySubGroups(data.getStatics().getIsSellOrderPublic().toString(), 4001));
		sellPartiesSubGroups1.add(createPartySubGroups(data.getStatics().getSellShortBrokerRef().toString(), 4002));
		sellParties.add(createParty(new String(data.getStatics().getSellFirmId().value), 1, sellPartiesSubGroups1));
		sellParties.add(createParty(new String(data.getStatics().getSellSponsoringFirmId().value), 7, null));
		List<PartiesSubGroup> sellPartiesSubGroups2 = new ArrayList<>();
		sellPartiesSubGroups2.add(createPartySubGroups(new String(data.getStatics().getSellGiveUpRef().value), 4003));
		sellParties.add(createParty(new String(data.getStatics().getSellGiveUpFirmId().value), 3, sellPartiesSubGroups2));
		sellParties.add(createParty(new String(data.getDynamics().getSellAccountChoice().getDepAccId().value), 10, null));
		sellOrdAlloc.setParties(sellParties);
		
		
		
		
		List<OrdAllocGrp> ordAllocGrps = new ArrayList<>();
		ordAllocGrps.add(buyOrdAlloc);
		ordAllocGrps.add(sellOrdAlloc);
		payload.setOrdAllocGrp(ordAllocGrps);
		if(data.getStatics().getTime().getDate() != null && data.getStatics().getTime().getTime() !=null && data.getStatics().getTime().getMsec() !=null )
		payload.setTransactTime(data.getStatics().getTime().getDate().toString()+"-"+data.getStatics().getTime().getTime().toString()+"."+data.getStatics().getTime().getMsec().toString());
		else if(data.getStatics().getTradeTime().getDate() !=null && data.getStatics().getTradeTime().getTime() != null && data.getStatics().getTradeTime().getMsec() != null)
			payload.setTransactTime(data.getStatics().getTradeTime().getDate().toString()+"-"+data.getStatics().getTradeTime().getTime().toString()+"."+data.getStatics().getTradeTime().getMsec().toString());
		
		
		Quote quote = new Quote();
		quote.setQuoteID((int)data.getStatics().getQuoteId().getQuoteNo().value); 
		payload.setQuote(quote);
		
		
		Instrument instrument = new Instrument();
		instrument.setSymbol(data.getStatics().getSecBoardId().getSecCode().toString());
		instrument.setSecuritySubType(data.getStatics().getSecBoardId().getBoardId().toString());
		instrument.setInstrumentID(data.getStatics().getInstrId().toString());
		instrument.setMaturityDate(data.getStatics().getMaturityDate().toString());
		payload.setInstrument(instrument);
		
		UserDefinedTags userDefinedTags = new UserDefinedTags();
		userDefinedTags.setTradeTypeIndicator(data.getStatics().getIsAutoTrade().toString());
		userDefinedTags.setDaysToSettlement((int)data.getStatics().getRepo().getRepoNofDays().value);
		userDefinedTags.setTradeSequence((int)data.getStatics().getRepo().getRepoTradeNo().value);
		userDefinedTags.setSecondaryTransactTime(data.getStatics().getRepo().getRepoAssociatedTradeId().getTradeDate().toString());
		userDefinedTags.setSettlCurrNetMoney(data.getStatics().getBaseCurrValue().getValue().value);
		userDefinedTags.setBTDSPriceChangeCode(data.getStatics().getIsLastPriceUpdated().value);
		userDefinedTags.setReportedPxDiff(data.getStatics().getIsMTP().toString());
		userDefinedTags.setOriginatorType(data.getStatics().getIsMarketMaker().toString());
		userDefinedTags.setExternalExchangeRef(data.getStatics().getCustomTradeId().toString());
		userDefinedTags.setFMTradeStatus(data.getDynamics().getStatus().value);
		userDefinedTags.setDisseminatedStatus(data.getDynamics().getDisseminationStatus().value);
		payload.setUserDefinedTags(userDefinedTags);
		
		if(data.getStatics().getPrice().getValue() != null)
		payload.setAvgPx((int)data.getStatics().getPrice().getValue().value);
		else if(data.getStatics().getRepoPrice().getValue() != null)
			payload.setAvgPx((int)data.getStatics().getRepoPrice().getValue().value);
		
		
		payload.setQuantity((int) data.getStatics().getQuantity().getValue().value);
		payload.setTradeInputSource(data.getStatics().getTradeSourceId().toString());
		
		YieldData yield = new YieldData();
		yield.setYield(data.getStatics().getDetails().getYield().getValue().value);
		payload.setYieldData(yield);
		payload.setAccruedInterestRate(data.getStatics().getDetails().getAccruedInterest().getValue().value);
		payload.setSettlDate(data.getStatics().getSettleDate().toString());
		
		
		if(data.getStatics().getReportOrderVerb().toString() !=null)
			payload.setSide(data.getStatics().getReportOrderVerb().toString());
		else if(data.getStatics().getIsSellShort() !=null)
			payload.setSide(data.getStatics().getIsSellShort().toString());
		
		TradeCaptureReport tradeCaptureReport = new TradeCaptureReport();
		tradeCaptureReport.setSecondaryTradeID(data.getStatics().getRepo().getRepoAssociatedTradeId().getTradeNo().value);
		payload.setTradeCaptureReport(tradeCaptureReport);
		payload.setTradingSessionID(data.getStatics().getSessionName().toString());
		payload.setAvgParPx(data.getStatics().getSecondaryPrice().getValue().value);
		
		
		
		return payload;
	}
	
	
	public Party createParty(String partyId, Integer partyRole,List<PartiesSubGroup> subgrps){
		Party party = new Party();
		party.setPartyId(Integer.valueOf(partyId));
		party.setPartyRole(partyRole);
		if(!subgrps.isEmpty()){
			party.setPartiesSubGroup(subgrps);
		}
		return party;
	}
	public PartiesSubGroup createPartySubGroups(String subId, Integer subType){
		PartiesSubGroup partiesSubGroup = new PartiesSubGroup();
		partiesSubGroup.setSubId(subId);
		partiesSubGroup.setSubType(subType);
		return partiesSubGroup;
	}
	
	
}
