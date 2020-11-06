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

import java.util.HashMap;
import java.util.List;

import com.axa.RockPaperScissorsChallengeState;
import com.axa.RockPaperScissorsFlows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @GetMapping(value = "/getnodes", produces = "text/html")
    private String getnodes() {
        List<NodeInfo> nodeinfos = this.proxy.networkMapSnapshot();
        NodeInfo thisNode = this.proxy.nodeInfo();
        NodeDiagnosticInfo thisNodeInfo = this.proxy.nodeDiagnosticInfo();
        List<CordappInfo> cordapps=thisNodeInfo.getCordapps();
        String reply=HTML_HEAD;
        reply+="<div style='max-width:300px; '>";
        boolean isCurrentNode=false;
        for (NodeInfo node : nodeinfos) {
            String nodeName=node.getLegalIdentities().get(0).toString();
            if (nodeName.equals(thisNode.getLegalIdentities().get(0).toString())) {
                isCurrentNode=true;
            }
            reply+="<div style='width:90%; border: solid black 1px; padding: 2px; margin: 4px; text-align:center'>";
            reply+="<div style='width:100%;'>"+nodeName+"</div>";
            reply+="<div style='width:100%;'>";
            reply+=node.getAddresses().get(0);
            reply+="</div>";
            reply+="<div style='width:100%; text-align:center'>\n";
            if (!isCurrentNode) {
                String counterParty=node.getLegalIdentities().get(0).toString();
                System.out.println("counterParty"+counterParty);
                counterParty=counterParty.substring(counterParty.indexOf('=')+1, counterParty.indexOf(','));
                if (counterParty.equals("Notary")) {
                    reply+="Notary";
                } else {
                    reply+="<a href='issue?counterParty="+counterParty+"&choice=rock'><div style='float: left;width:30%; text-align:center'>ROCK</div></a>"+
                    "<a href='issue?counterParty="+counterParty+"&choice=paper'><div style='float: left;width:30%; text-align:center'>PAPER</div></a>"+
                    "<a href='issue?counterParty="+counterParty+"&choice=scissors'><div style='display:inline-block;width:30%; text-align:center'>SCISSORS</div></a>";
                }
            }
            reply+="</div></div>";
            isCurrentNode=false;
        }
        reply+="</div>"+HTML_FOOT;
        System.out.println("All nodes "+nodeinfos);
        System.out.println("this node "+thisNode);
        System.out.println("this node diagnostic "+thisNodeInfo);
        return reply;
    }
    @GetMapping(value = "/issue", produces = "text/html")
    private String issue(@RequestParam String counterParty,@RequestParam String choice) {
        String reply=HTML_HEAD;
        System.out.println("issue "+counterParty);
        Party party=getParty(counterParty);
        try {
            SignedTransaction result = this.proxy.startFlowDynamic(RockPaperScissorsFlows.ChallengeFlow.class, party, choice).getReturnValue().get();
            reply+="<div style:'max-width:320px; text-align:center'>Transaction id "+ result.getId() +" <br/>committed to ledger.\n " + result.getTx().getOutput(0)+"</div>";
        } catch (Exception e) {
            reply+="<div style:'max-width:320px; text-align:center'>Exception "+ e +"</div>";
        }
        reply+=HTML_FOOT;
        return reply;

    }

    @GetMapping(value = "/gettransactions", produces = "text/html")
    private String getTransactions() {
        String reply=HTML_HEAD;
        Page<RockPaperScissorsChallengeState> vaultQuery = this.proxy.vaultQuery(RockPaperScissorsChallengeState.class);
        List<StateAndRef<RockPaperScissorsChallengeState>> states=vaultQuery.getStates();
        List<StateMetadata> statesMetaData=vaultQuery.getStatesMetadata();
        System.out.println("statesMetaData"+statesMetaData);
        reply+="<div style=''>";
        for (StateAndRef<RockPaperScissorsChallengeState> state : states) {
            HashMap<String, String> map=stateToMap(state);
            reply+="<div style='border: solid black 1px; padding: 2px; margin: 4px; text-align:center'>";
            for (String key : map.keySet()) {
                reply+="<div style='border-bottom:solid black 1px'>"+key+":";
                reply+=map.get(key)+"</div><br/>";
            }
            reply+="<div style='cursor:pointer' class='myDIV'>...</div>";
            String metaString="";
            for (StateMetadata metadata : statesMetaData) {
                if (metadata.getRef().getTxhash().equals(state.getRef().getTxhash())) {
                    metaString=metadata.toString();
                    break;
                }
            }
            reply+="<div class='hide'>"+metaString+"<br/>"+
            state.referenced().toString()+"</div>";
            reply+="</div>";
        }
        reply+="</div>";
        reply+="<style>\n"+
        ".hide {\n"+
        "  display: none;\n"+
        "}\n"+
        "\n"+
        ".myDIV:hover + .hide {\n"+
        "  display: block;\n"+
        "  position: fixed;\n"+
        "  background-color: white;\n"+
        "  border: solid black 1px;\n"+
        "  padding: 4px;\n"+
        "}\n"+
        "</style>";
        reply+=HTML_FOOT;
        return reply;
    }
    private HashMap<String, String> stateToMap(StateAndRef<RockPaperScissorsChallengeState> state) {
        HashMap<String, String> map=new HashMap<String, String>();
        map.put("hash",state.getRef().getTxhash().toString().substring(0,32)+"...");
        map.put("notary", state.getState().getNotary().getName().toString());
        map.put("challenged", state.getState().getData().getChallenged().getName().toString());
        map.put("challenger", state.getState().getData().getChallenger().getName().toString());
        map.put("challenger choice", state.getState().getData().getChallengerChoice());

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