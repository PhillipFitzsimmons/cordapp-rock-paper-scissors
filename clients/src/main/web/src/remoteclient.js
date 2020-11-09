const LOCAL_SERVER="http://localhost:10050/";
const GET_NODES_API="getnodes";
const ISSUE_API="issue";
const GET_TRANSACTIONS_API="gettransactions";

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
            return json
        }).catch(err=>{
            console.log("getNodes error", err);
        })
        console.log("getNodes", reply);
    callback(reply);
}
export const sendChallenge =  async(gameboard, callback) => {
    let url=`${getServer()}${ISSUE_API}?counterParty=${gameboard.counterParty}&escrow=${gameboard.escrow}&choice=${gameboard.choice}`;
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
            return json
        }).catch(err=>{
            console.log("sendChallenge error", err);
        })
        console.log("sendChallenge", reply);
    callback(reply);
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
            let responseCode = response.status;
            return response.json();

        })
        .then(function (json) {
            return json
        }).catch(err=>{
            console.log("getTransactions error", err);
        })
        console.log("getTransactions", reply);
    callback(reply);
}

