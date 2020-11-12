package com.template.webserver;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.cordapp.CordappInfo;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeDiagnosticInfo;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.Vault.Page;
import net.corda.core.node.services.Vault.StateMetadata;
import net.corda.core.node.services.Vault.StateStatus;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.node.services.vault.Sort;
import net.corda.core.node.services.vault.SortAttribute;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.NetworkHostAndPort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axa.RockPaperScissorsAcceptedState;
import com.axa.RockPaperScissorsChallengeState;
import com.axa.RockPaperScissorsFlows;
import com.axa.RockPaperScissorsIssuedState;
import com.axa.RockPaperScissorsSettledState;

import org.apache.activemq.artemis.utils.ByteUtil;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
    }
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/getnodes", produces = "application/json")
    private List getnodes() {
        List<NodeInfo> nodeinfos = this.proxy.networkMapSnapshot();
        NodeInfo thisNode = this.proxy.nodeInfo();
        NodeDiagnosticInfo thisNodeInfo = this.proxy.nodeDiagnosticInfo();
        List<CordappInfo> cordapps=thisNodeInfo.getCordapps();
        List list=new ArrayList<Map>();
        for (NodeInfo node : nodeinfos) {
            System.out.println("node"+node);
            Map<String, Object> map=new HashMap<String, Object>();
            ArrayList<Map<String, Object>> identities=new ArrayList<Map<String, Object>>();
            for (Party party : node.getLegalIdentities()) {
                Map<String, Object> idMap=new HashMap<String, Object>();
                idMap.put("name", party.getName());
                idMap.put("owningKey", party.getOwningKey().toString());
                identities.add(idMap);
            }
            map.put("identities", identities);
            ArrayList<String> addresses=new ArrayList<String>();
            for (NetworkHostAndPort nhost : node.getAddresses()) {
                addresses.add(nhost.toString());
            }
            map.put("addresses", addresses);
            map.put("isCurrentNode",""+thisNode.getLegalIdentities().get(0).toString().equals(node.getLegalIdentities().get(0).toString()));
            List<PartyAndCertificate> parties=node.getLegalIdentitiesAndCerts();
            List<Map> certs=new ArrayList<Map>();
            for (PartyAndCertificate pac : parties) {
                Map<String, String> certificateMap=new HashMap<String, String>();
                certificateMap.put("principal", pac.getCertificate().getSubjectX500Principal().getName());
                certificateMap.put("issuer", pac.getCertificate().getIssuerX500Principal().getName());
                //certificateMap.put("owningKey", pac.getOwningKey().);
                certificateMap.put("name", pac.getName().getCommonName());
                certificateMap.put("organization", pac.getName().getOrganisation());
                certificateMap.put("country", pac.getName().getCountry());
                certificateMap.put("subect", pac.getCertificate().getSubjectX500Principal().getName());
                certs.add(certificateMap);
            }
            map.put("certificates", certs);
            list.add(map);
        }
        return list;
    }
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/issue", produces = "application/json")
    private Map<String, String> issue(@RequestParam String counterParty,@RequestParam String escrowParty,@RequestParam String choice) {
        System.out.println("/issue challenged:"+counterParty+" escrow:"+escrowParty);
        Party party=getParty(counterParty);
        Party escrow=getParty(escrowParty);
        System.out.println("/issue Parties challenged:"+party.toString()+" escrow:"+escrow.toString());
        Map<String, String> reply=new HashMap<String, String>();
        try {
            UniqueIdentifier uniqueIdentifier= this.proxy.startFlowDynamic(RockPaperScissorsFlows.IssueFlow.class, party, escrow, choice).getReturnValue().get();
            reply.put("uniqueIdentifier", uniqueIdentifier.toString());
            //reply.put("committed", result.getTx().getOutput(0).toString());
        } catch (Exception e) {
            reply.put("error", e.getMessage());

        }
        return reply;

    }
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/accept", produces = "application/json")
    private Map<String, String> accept(@RequestParam String linearId,@RequestParam String escrowParty,@RequestParam String challenged,@RequestParam String choice) {
        Party party=getParty(challenged);
        Party escrow=getParty(escrowParty);
        Map<String, String> reply=new HashMap<String, String>();
        System.out.println("/accept linearId "+linearId);
        try {
            UniqueIdentifier uniqueIdentifier= this.proxy.startFlowDynamic(RockPaperScissorsFlows.AcceptChallengeFlow.class, linearId, party, escrow, choice).getReturnValue().get();
            reply.put("uniqueIdentifier", uniqueIdentifier.toString());
        } catch (Exception e) {
            reply.put("error", e.getMessage());
        }
        return reply;

    }
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/gettransactions", produces = "application/json")
    private List<Map<String, Object>> getStates() {
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(StateStatus.ALL);
        Page<LinearState> vaultQuery=this.proxy.vaultQueryByCriteria(criteria, LinearState.class);
        List<StateAndRef<LinearState>> states=vaultQuery.getStates();
        List<StateMetadata> statesMetaData=vaultQuery.getStatesMetadata();
        List<Map<String, Object>> result=new ArrayList<Map<String, Object>>();
        for (StateAndRef<LinearState> state : states) {
            Map<String, Object> map=new HashMap<String, Object>();
            map.put("id", state.getState().getData().getLinearId().getExternalId()!=null ? state.getState().getData().getLinearId().getExternalId() : state.getState().getData().getLinearId().getId().toString());
            mapMetaData(state, map, statesMetaData);
            String slass=state.getState().getData().getClass()+"";
            if (slass.indexOf(' ')>-1) {
                slass=slass.substring(slass.indexOf(' ')+1, slass.length());
            }
            map.put("class", slass);
            result.add(map);
        }
        return result;
    }
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/getstatesbylinearid", produces = "application/json")
    private Map<String, Map<String, Object>> getStatesByLinearId(@RequestParam String linearId) {
        
        //QueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria( null, Arrays.asList(java.util.UUID.fromString(linearId)),  null,  Vault.StateStatus.ALL,  null);
        //Page<LinearState> vaultQuery=this.proxy.vaultQueryByCriteria(criteria, LinearState.class);

        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(StateStatus.ALL);
        Page<LinearState> vaultQuery=this.proxy.vaultQueryByCriteria(criteria, LinearState.class);


        List<StateAndRef<LinearState>> states=vaultQuery.getStates();
        List<StateMetadata> statesMetaData=vaultQuery.getStatesMetadata();
        System.out.println("getStatesByLinearId states "+states);
        Map<String, Map<String, Object>> result= new HashMap<String, Map<String, Object>>();
        for (StateAndRef<LinearState> state : states) {
            Map<String, Object> map=new HashMap<String, Object>();
            LinearState linearState=state.getState().getData();
            String testId=linearState.getLinearId().getExternalId()!=null ? linearState.getLinearId().getExternalId()+"" : linearState.getLinearId().getId()+"";
            //I haven't figured out linear ID yet - sometimes it's longer, sometimes shorter
            //So rather than querying for it I'm getting everything and filtering here.
            //Obviously this is a big TODO, but this is a POC
            if (!testId.equals(linearId)) {
                System.out.println("Skipping "+linearState+" because testId "+testId+"!="+linearId);
                continue;
            }
            if (linearState instanceof RockPaperScissorsIssuedState) {
                RockPaperScissorsIssuedState issuedState=(RockPaperScissorsIssuedState)linearState;
                map.put("challenged",partyToMap(issuedState.getChallenged()));
                map.put("challenger",partyToMap(issuedState.getChallenger()));
                map.put("challengerChoice",issuedState.getChallengerChoice());
                map.put("escrow",partyToMap(issuedState.getEscrow()));
                result.put("IssuedState", map);
            } else if (linearState instanceof RockPaperScissorsChallengeState) {
                RockPaperScissorsChallengeState issuedState=(RockPaperScissorsChallengeState)linearState;
                map.put("challenged",partyToMap(issuedState.getChallenged()));
                map.put("challenger",partyToMap(issuedState.getChallenger()));
                map.put("escrow",partyToMap(issuedState.getEscrow()));
                result.put("ChallengeState", map);
            } else if (linearState instanceof RockPaperScissorsAcceptedState) {
                RockPaperScissorsAcceptedState issuedState=(RockPaperScissorsAcceptedState)linearState;
                map.put("challenged",partyToMap(issuedState.getChallenged()));
                map.put("challenger",partyToMap(issuedState.getChallenger()));
                map.put("escrow",partyToMap(issuedState.getEscrow()));
                map.put("challengedChoice",issuedState.getChallengedChoice());
                result.put("AcceptedState", map);
            } else if (linearState instanceof RockPaperScissorsSettledState) {
                RockPaperScissorsSettledState issuedState=(RockPaperScissorsSettledState)linearState;
                map.put("challenged",partyToMap(issuedState.getChallenged()));
                map.put("challenger",partyToMap(issuedState.getChallenger()));
                map.put("escrow",partyToMap(issuedState.getEscrow()));
                map.put("challengedChoice",issuedState.getChallengedChoice());
                map.put("challengerChoice",issuedState.getChallengerChoice());
                map.put("winner",partyToMap(issuedState.getWinner()));
                result.put("SettledState", map);
            }
            map.put("linearId",linearState.getLinearId()+"");
            mapMetaData(state, map, statesMetaData);
        }

        Map<String, Object> thisNodeMap=new HashMap<String, Object>();
        thisNodeMap.put("name", this.proxy.nodeInfo().getLegalIdentities().get(0).toString());
        result.put("currentNode",thisNodeMap);
        return result;
    }
    private Map<String, String> partyToMap(Party party) {
        if (party!=null) {
            Map<String, String> map=new HashMap<String, String>();
            map.put("name", party.nameOrNull().toString());
            map.put("publicKey", ByteUtil.bytesToHex(party.getOwningKey().getEncoded()));
            return map;
        }
        return null;
    }
    private void mapMetaData(StateAndRef<LinearState> state, Map<String, Object> map, List<StateMetadata> statesMetaData) {
            for (StateMetadata metadata : statesMetaData) {
                if (metadata.getRef().getTxhash().equals(state.getRef().getTxhash())) {
                    //map.put("metadata", metadata.toString());
                    map.put("ref", metadata.getRef().toString());
                    map.put("recordedTime", metadata.getRecordedTime()!=null ? metadata.getRecordedTime().toEpochMilli()+"" : "");
                    map.put("status", metadata.getStatus().toString());
                    Map<String, String> notaryMap=new HashMap<String, String>();
                    notaryMap.put("name", metadata.getNotary().nameOrNull().toString());
                    notaryMap.put("publicKey", metadata.getNotary().getOwningKey().toString());
                    map.put("notary", notaryMap);
                    map.put("lockUpdateTime", metadata.getLockUpdateTime()!=null ? metadata.getLockUpdateTime().toEpochMilli()+"":"");
                    map.put("relevancyStatus", metadata.getRelevancyStatus().toString());
                    map.put("constraint", metadata.getConstraintInfo().toString());
                    map.put("consumedTime", metadata.getConsumedTime()!=null ? metadata.getConsumedTime().toEpochMilli()+"":"");
                    break;
                }
        }
    }
    private HashMap<String, Object> stateToMap(StateAndRef<RockPaperScissorsChallengeState> state) {
        HashMap<String, Object> map=new HashMap<String, Object>();
        map.put("hash",state.getRef().getTxhash().toString());
        map.put("notary", state.getState().getNotary().getName().toString());
        Map<String, String> challengedMap=new HashMap<String, String>();
        challengedMap.put("commonName", state.getState().getData().getChallenged().getName().getCommonName());
        challengedMap.put("organisation", state.getState().getData().getChallenged().getName().getOrganisation());
        challengedMap.put("organisationUnit", state.getState().getData().getChallenged().getName().getOrganisationUnit());
        challengedMap.put("country", state.getState().getData().getChallenged().getName().getCountry());
        challengedMap.put("principal", state.getState().getData().getChallenged().getName().getX500Principal().getName());
        map.put("counterParty", challengedMap);
        Map<String, String> challengerMap=new HashMap<String, String>();
        challengerMap.put("commonName", state.getState().getData().getChallenger().getName().getCommonName());
        challengerMap.put("organisation", state.getState().getData().getChallenger().getName().getOrganisation());
        challengerMap.put("organisationUnit", state.getState().getData().getChallenger().getName().getOrganisationUnit());
        challengerMap.put("country", state.getState().getData().getChallenger().getName().getCountry());
        challengerMap.put("principal", state.getState().getData().getChallenger().getName().getX500Principal().getName());
        map.put("party", challengerMap);
        //map.put("challengerChoice", state.getState().getData().getChallengerChoice());

        return map;
    }

    private Party getParty(String counterParty) {
        List<NodeInfo> nodeinfos = this.proxy.networkMapSnapshot();
        Party party=null;
        for (NodeInfo node : nodeinfos) {
            String nodeName=node.getLegalIdentities().get(0).toString();
            if (nodeName.indexOf(counterParty)>-1) {
                List<PartyAndCertificate> certs=node.getLegalIdentitiesAndCerts();
                System.out.println("certs "+certs);
                party=new Party(certs.get(0).getCertificate());
            }
        }
        System.out.println("getParty"+party);
        return party;
    }
}