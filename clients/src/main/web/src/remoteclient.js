const LOCAL_SERVER="http://localhost:10050/";
const GET_NODES_API="getnodes";
const ISSUE_API="issue";
const GET_TRANSACTIONS_API="gettransactions";
const GET_TRANSACTION_DETAILS_API="getstatesbylinearid";
const ACCEPT_CHALLENGE_API="accept";

function getServer() {
    console.log("getServer", window.location.href);
    if (window.location.href.indexOf('3000')>-1) {
        return LOCAL_SERVER;
    } else {
        console.log("getServer returning", window.location.href);
        return window.location.href;
    }
}

export const getNodes = async(callback) => {
    let url=`${getServer()}${GET_NODES_API}`;
    console.log("getNodes url", url);
    let reply = await fetch(url, {
        method: "GET",
        credentials: "same-origin", // send cookies
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
    })
        .then(function (response) {
            let responseCode = response.status;
            return response.json();

        })
        .then(function (json) {
            return {response:json}
        }).catch(err=>{
            console.log("getNodes error", err);
            return {error: `"${err}"`}
        })
        console.log("getNodes", reply);
    callback(reply.error, reply.response);
}
export const sendChallenge = async(gameboard, callback) => {
    console.log("sendChallenge", gameboard);
    let url=`${getServer()}${ISSUE_API}?counterParty=${gameboard.counterParty}&escrowParty=${gameboard.escrow}&choice=${gameboard.choice}`;
    let reply = await fetch(url, {
        method: "GET",
        credentials: "same-origin", // send cookies
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
    })
        .then(function (response) {
            //let responseCode = response.status;
            return response.json();
        })
        .then(function (json) {
            //return json
            return {response:json}
        }).catch(err=>{
            console.log("sendChallenge error", err);
            return {error: `"${err}"`}
        })
        console.log("sendChallenge", reply);
    //callback(reply);
    callback(reply.error, reply.response);
}

export const sendAcceptChallenge =  async(challenge, callback) => {
    console.log("sendAcceptChallenge", challenge);
    let url=`${getServer()}${ACCEPT_CHALLENGE_API}?linearId=${challenge.linearId}&choice=${challenge.choice}&escrowParty=${challenge.escrow.name}&challenged=${challenge.challenged.name}`;
    console.log("sendAcceptChallenge", url);
    let reply = await fetch(url, {
        method: "GET",
        credentials: "same-origin", // send cookies
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
    })
        .then(function (response) {
            //let responseCode = response.status;
            return response.json();

        })
        .then(function (json) {
            //return json
            return {response:json}
        }).catch(err=>{
            console.log("sendAcceptChallenge error", err);
            return {error: `"${err}"`}
        })
        console.log("sendAcceptChallenge", reply);
    //callback(reply);
    callback(reply.error, reply.response);
}
export const getTransactions = async(callback) => {
    let url=`${getServer()}${GET_TRANSACTIONS_API}`;
    console.log("getTransactions url", url);
    let reply = await fetch(url, {
        method: "GET",
        credentials: "same-origin", // send cookies
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
    })
        .then(function (response) {
            //let responseCode = response.status;
            return response.json();

        })
        .then(function (json) {
            //return json
            return {response:json}
        }).catch(err=>{
            console.log("getTransactions error", err);
            return {error: `"${err}"`}
        })
        console.log("getTransactions", reply);
    //callback(reply);
    callback(reply.error, reply.response);
}
export const getTransactionDetails = async(transaction, callback) => {
    let url=`${getServer()}${GET_TRANSACTION_DETAILS_API}?linearId=${transaction.id}`;
    console.log("getTransactionDetails url", url);
    let reply = await fetch(url, {
        method: "GET",
        credentials: "same-origin", // send cookies
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
    })
        .then(function (response) {
            //let responseCode = response.status;
            return response.json();

        })
        .then(function (json) {
            //return json
            return {response:json}
        }).catch(err=>{
            console.log("getTransactionDetails error", err);
            return {error: `"${err}"`}
        })
        console.log("getTransactionDetails", reply);
    //callback(reply);
    callback(reply.error, reply.response);
}

