package com.template.webserver;

import net.corda.core.contracts.StateAndRef;
import net.corda.core.cordapp.CordappInfo;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeDiagnosticInfo;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault.Page;
import net.corda.core.node.services.Vault.StateMetadata;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.NetworkHostAndPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axa.RockPaperScissorsChallengeState;
import com.axa.RockPaperScissorsFlows;

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
    private Map issue(@RequestParam String counterParty,@RequestParam String escrow,@RequestParam String choice) {
        Party party=getParty(counterParty);
        Map<String, String> reply=new HashMap<String, String>();
        try {
            SignedTransaction result = this.proxy.startFlowDynamic(RockPaperScissorsFlows.ChallengeFlow.class, party, choice).getReturnValue().get();
            reply.put("txId", result.getId().toString());
            reply.put("committed", result.getTx().getOutput(0).toString());
        } catch (Exception e) {
            reply.put("error", e.getMessage());

        }
        return reply;

    }
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/gettransactions", produces = "application/json")
    private List<Map> getTransactions() {
        Page<RockPaperScissorsChallengeState> vaultQuery = this.proxy.vaultQuery(RockPaperScissorsChallengeState.class);
        List<StateAndRef<RockPaperScissorsChallengeState>> states=vaultQuery.getStates();
        List<StateMetadata> statesMetaData=vaultQuery.getStatesMetadata();
        System.out.println("statesMetaData"+statesMetaData);
        List<Map> reply=new ArrayList<Map>();
        for (StateAndRef<RockPaperScissorsChallengeState> state : states) {
            HashMap<String, Object> map=stateToMap(state);
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
            reply.add(map);
        }
        return reply;
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
        map.put("challengerChoice", state.getState().getData().getChallengerChoice());

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

    private static final String HTML_HEAD="<html><body style='text-align:center'>\n"+
    "<meta name='viewport' content='width=device-width, initial-scale=1'>\n"+
    "<link rel='stylesheet' href='https://www.w3schools.com/w3css/4/w3.css'>\n"+
    "<link rel='stylesheet' href='https://www.w3schools.com/lib/w3-theme-black.css'>\n"+
    "<link rel='stylesheet' href='https://fonts.googleapis.com/css?family=Roboto'>\n"+
    "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css'>\n"+
    "<a href='/getnodes'><div style='width:49%;text-align:center;float:left'>nodes</div></a>\n"+
    "<a href='/gettransactions'><div style='width:49%;text-align:center;display:inline-block'>transactions</div></a>\n";
    private static final String HTML_FOOT="\n</body></html>";
}