(this.webpackJsonpweb=this.webpackJsonpweb||[]).push([[0],{101:function(e,n,t){},102:function(e,n,t){},184:function(e,n,t){"use strict";t.r(n);var a=t(4),c=t(0),o=t.n(c),r=t(10),i=t.n(r),s=(t(101),t(13)),l=t(40),d=t(16),u=(t.p,t(3)),j=(t(102),t(226)),b=t(18),h=t(238),p=t(63),f=t.n(p),O=t(81),x=t.n(O),m=t(82),g=t.n(m),v=t(78),y=t.n(v),C=t(237),N=t(235),S=t(241),w=t(236),k=t(55),E=t(240),T=t(56),A=t(80),P=t.n(A),G=t(228),I=t(187),D=t(231),F=t(230),W=t(242),B=t(52),J=t.n(B),L=t(61),M=t.n(L),U=t(62),H=t.n(U),R=t(35),q=t.n(R),z=t(54);function K(){return window.location.href.indexOf("3000")>-1?"http://localhost:10050/":window.location.href}var Q=function(){var e=Object(z.a)(q.a.mark((function e(n){var t,a;return q.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return t="".concat(K()).concat("getnodes"),alert(t),e.next=4,fetch(t,{method:"GET",credentials:"same-origin",headers:{Accept:"application/json, text/plain, */*","Content-Type":"application/json"}}).then((function(e){e.status;return e.json()})).then((function(e){return e}));case 4:a=e.sent,console.log("getNodes",a),n(a);case 7:case"end":return e.stop()}}),e)})));return function(n){return e.apply(this,arguments)}}(),V=function(){var e=Object(z.a)(q.a.mark((function e(n,t){var a,c;return q.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return a="".concat(K()).concat("issue","?counterParty=").concat(n.counterParty,"&escrow=").concat(n.escrow,"&choice=").concat(n.choice),e.next=3,fetch(a,{method:"GET",credentials:"same-origin",headers:{Accept:"application/json, text/plain, */*","Content-Type":"application/json"}}).then((function(e){e.status;return e.json()})).then((function(e){return e}));case 3:c=e.sent,console.log("sendChallenge",c),t(c);case 6:case"end":return e.stop()}}),e)})));return function(n,t){return e.apply(this,arguments)}}(),X=function(){var e=Object(z.a)(q.a.mark((function e(n){var t,a;return q.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return t="".concat(K()).concat("gettransactions"),e.next=3,fetch(t,{method:"GET",credentials:"same-origin",headers:{Accept:"application/json, text/plain, */*","Content-Type":"application/json"}}).then((function(e){e.status;return e.json()})).then((function(e){return e}));case 3:a=e.sent,console.log("getTransactions",a),n(a);case 6:case"end":return e.stop()}}),e)})));return function(n){return e.apply(this,arguments)}}(),Y=Object(j.a)((function(e){return{root:{width:"100%",maxWidth:360,backgroundColor:e.palette.background.paper}}}));var Z=function(e){var n=e.onTransactionSelected,t=(Object(d.a)(e,["onTransactionSelected"]),Y()),c=o.a.useState([]),r=Object(s.a)(c,2),i=r[0],l=r[1];return o.a.useEffect((function(){X((function(e){console.log("transactions",e),l(e)}))}),[]),Object(a.jsx)(G.a,{className:t.root,children:i&&i.map((function(e,t){return Object(a.jsxs)(I.a,{onClick:function(){return n(e)},children:[Object(a.jsx)(F.a,{children:Object(a.jsxs)(W.a,{children:["tbd"==e.status&&Object(a.jsx)(M.a,{}),"tbd"==e.status&&Object(a.jsx)(J.a,{}),"UNCONSUMED"==e.status&&Object(a.jsx)(H.a,{})]})}),Object(a.jsx)(D.a,{primary:e.challengerChoice+" "+e.counterParty.organisation,secondary:new Date(1*e.recordedTime).toISOString()})]})}))})},$=t(79),_=t.n($),ee=Object(j.a)((function(e){return{root:{width:"100%",maxWidth:360,backgroundColor:e.palette.background.paper}}}));var ne=function(e){var n=e.onNodeSelected,t=(Object(d.a)(e,["onNodeSelected"]),ee()),c=o.a.useState(""),r=Object(s.a)(c,2),i=r[0],l=r[1];return o.a.useEffect((function(){Q((function(e){console.log("NodeExplorer",e);for(var n=0;n<e.length;n++)e[n].name=e[n].identities[0].name.organisation,e[n].address=e[n].addresses[0];l(e)}))}),[]),Object(a.jsx)(G.a,{className:t.root,children:i&&i.map((function(e,t){return Object(a.jsxs)(I.a,{onClick:function(){return n(e)},children:[Object(a.jsx)(F.a,{children:Object(a.jsxs)(W.a,{children:["Notary"!=e.name&&Object(a.jsx)(_.a,{}),"Notary"==e.name&&Object(a.jsx)(J.a,{})]})}),Object(a.jsx)(D.a,{primary:e.name,secondary:e.address})]})}))})},te=(t(72),t(53)),ae=t.n(te);var ce=function(e){var n=e.node;return Object(d.a)(e,["node"]),Object(a.jsx)("div",{style:{textAlign:"left"},children:Object(a.jsx)(ae.a,{src:n})})},oe=t(243),re=t(233),ie=t(234),se=t(232),le=t(239),de=Object(j.a)((function(e){return{formControl:{margin:e.spacing(1),minWidth:"90%"},selectEmpty:{marginTop:e.spacing(2)}}}));function ue(e){var n=e.onChange,t=(Object(d.a)(e,["onChange"]),de()),c=o.a.useState(""),r=Object(s.a)(c,2),i=r[0],l=r[1],u=o.a.useState(""),j=Object(s.a)(u,2),b=j[0],h=j[1],p=o.a.useState(""),f=Object(s.a)(p,2),O=f[0],x=f[1],m=o.a.useState(""),g=Object(s.a)(m,2),v=g[0],y=g[1],C=o.a.useState(""),N=Object(s.a)(C,2),S=N[0],w=N[1];return o.a.useEffect((function(){Q((function(e){console.log("Gameboard",e);for(var n=0;n<e.length;n++)e[n].name=e[n].identities[0].name.organisation;y(e)}))}),[]),o.a.useEffect((function(){var e=!1;if(O&&b&&i)if(b===i)w("Escrow and counterparty cannot be the same party");else{var t=v.find((function(e){return e.isCurrentNode}));t.name===b?w("Counterparty and current node cannot be the same node"):t.name===i?w("Escrow and current node cannot be the same node"):"Notary"===b||"Notary"===i?w("Notary cannot be a signing party"):(w(!1),e=!0)}n&&n({ready:e,escrow:i,counterParty:b,choice:O})}),[b,i,O]),Object(a.jsxs)("div",{children:[Object(a.jsxs)(se.a,{className:t.formControl,children:[Object(a.jsx)(oe.a,{id:"demo-simple-select-label",children:"Choice"}),Object(a.jsxs)(le.a,{labelId:"demo-simple-select-label",id:"demo-simple-select",value:O,onChange:function(e){x(e.target.value)},children:[Object(a.jsx)(re.a,{value:"rock",children:"Rock"}),Object(a.jsx)(re.a,{value:"paper",children:"Paper"}),Object(a.jsx)(re.a,{value:"scissors",children:"Scissors"})]})]}),Object(a.jsxs)(se.a,{className:t.formControl,error:S,children:[Object(a.jsx)(oe.a,{id:"demo-simple-select-label",children:"Escrow"}),Object(a.jsx)(le.a,{labelId:"demo-simple-select-label",id:"demo-simple-select",value:i,onChange:function(e){l(e.target.value)},children:v&&v.map((function(e){return Object(a.jsx)(re.a,{value:e.name,children:e.name})}))})]}),Object(a.jsxs)(se.a,{className:t.formControl,error:S,children:[Object(a.jsx)(oe.a,{id:"demo-simple-select-label",children:"Counter Party"}),Object(a.jsx)(le.a,{labelId:"demo-simple-select-label",id:"demo-simple-select",value:b,onChange:function(e){h(e.target.value)},children:v&&v.map((function(e){return Object(a.jsx)(re.a,{value:e.name,children:e.name})}))})]}),Object(a.jsx)(ie.a,{style:{display:S?"":"none"},children:S})]})}var je=function(e){var n=e.transaction;return Object(d.a)(e,["transaction"]),Object(a.jsx)("div",{style:{textAlign:"left"},children:Object(a.jsx)(ae.a,{src:n})})};function be(e){var n=e.children,t=e.value,c=e.index,o=Object(d.a)(e,["children","value","index"]);return Object(a.jsx)(T.a,Object(l.a)(Object(l.a)({component:"div",role:"tabpanel",hidden:t!==c,id:"action-tabpanel-".concat(c),"aria-labelledby":"action-tab-".concat(c)},o),{},{children:t===c&&Object(a.jsx)(E.a,{p:3,children:n})}))}var he=Object(j.a)((function(e){return{root:{backgroundColor:e.palette.background.paper,width:500,position:"relative",minHeight:200},fab:{position:"absolute",bottom:e.spacing(2),right:e.spacing(2)},fabGreen:{color:e.palette.common.white,backgroundColor:k.a[500],"&:hover":{backgroundColor:k.a[600]}}}}));function pe(e){return{id:"action-tab-".concat(e),"aria-controls":"action-tabpanel-".concat(e)}}var fe=function(){var e=o.a.useState(0),n=Object(s.a)(e,2),t=n[0],c=n[1],r=o.a.useState(!1),i=Object(s.a)(r,2),d=i[0],j=i[1],p=o.a.useState({}),O=Object(s.a)(p,2),m=O[0],v=O[1],k=o.a.useState({}),E=Object(s.a)(k,2),T=E[0],A=E[1],G=o.a.useState({}),I=Object(s.a)(G,2),D=I[0],F=I[1],W=function(){c(3)};o.a.useEffect((function(){console.log("value",t),2!=t&&(A(null),F(null))}),[t]);var B=he(),J=Object(b.a)(),L={enter:J.transitions.duration.enteringScreen,exit:J.transitions.duration.leavingScreen},M=[{color:"primary",className:B.fab,icon:Object(a.jsx)(f.a,{}),label:"Add",action:W,color2:"secondary",display2:"none"},{color:"primary",className:B.fab,icon:Object(a.jsx)(f.a,{}),label:"Add",action:W,color2:"secondary",display2:"none"},{color:"inherit",className:Object(u.a)(B.fab,B.fabGreen),icon:Object(a.jsx)(P.a,{}),label:"Expand",display:"none",display2:"none"},{color:"primary",className:Object(u.a)(B.fab,B.fabGreen),disabled:!d,icon:Object(a.jsx)(x.a,{}),icon2:Object(a.jsx)(g.a,{}),label:"Edit",action:function(){V(m,(function(e){console.log("challenge issued",e),c(3)}))},className2:Object(u.a)(B.fab),color2:"secondary",action2:function(){c(0)}}];return Object(a.jsxs)("div",{className:"App",children:[Object(a.jsx)(N.a,{position:"static",color:"default",children:Object(a.jsxs)(S.a,{value:t,onChange:function(e,n){c(n)},indicatorColor:"primary",textColor:"primary",variant:"fullWidth","aria-label":"action tabs example",children:[Object(a.jsx)(w.a,Object(l.a)({label:"Transactions"},pe(0))),Object(a.jsx)(w.a,Object(l.a)({label:"Nodes"},pe(1))),Object(a.jsx)(w.a,Object(l.a)({label:T?T.name:D?D.challengerChoice:""},pe(2)))]})}),Object(a.jsxs)(y.a,{axis:"rtl"===J.direction?"x-reverse":"x",index:t,onChangeIndex:function(e){c(e)},children:[Object(a.jsx)(be,{value:t,index:0,dir:J.direction,children:Object(a.jsx)(Z,{onTransactionSelected:function(e){F(e),c(2)}})}),Object(a.jsx)(be,{value:t,index:1,dir:J.direction,children:Object(a.jsx)(ne,{onNodeSelected:function(e){A(e),c(2)}})}),Object(a.jsxs)(be,{value:t,index:2,dir:J.direction,children:[T&&Object(a.jsx)(ce,{node:T}),D&&Object(a.jsx)(je,{transaction:D})]}),Object(a.jsx)(be,{value:t,index:3,dir:J.direction,children:Object(a.jsx)(ue,{onChange:function(e){j(e.ready),v(e)}})})]}),M.map((function(e,n){return Object(a.jsxs)(a.Fragment,{children:[Object(a.jsx)(C.a,{in:t===n,timeout:L,style:{transitionDelay:"".concat(t===n?L.exit:0,"ms")},unmountOnExit:!0,children:Object(a.jsx)(h.a,{"aria-label":e.label,className:e.className,color:e.color,style:{display:e.display},disabled:e.disabled,onClick:e.action,children:e.icon})},e.color),Object(a.jsx)(C.a,{in:t===n,timeout:L,style:{transitionDelay:"".concat(t===n?L.exit:0,"ms")},unmountOnExit:!0,children:Object(a.jsx)(h.a,{"aria-label":e.label,className:e.className2,color:e.color2,style:{display:e.display2,right:"80px"},onClick:e.action2,children:e.icon2})},e.color)]})}))]})},Oe=function(e){e&&e instanceof Function&&t.e(3).then(t.bind(null,245)).then((function(n){var t=n.getCLS,a=n.getFID,c=n.getFCP,o=n.getLCP,r=n.getTTFB;t(e),a(e),c(e),o(e),r(e)}))};i.a.render(Object(a.jsx)(o.a.StrictMode,{children:Object(a.jsx)(fe,{})}),document.getElementById("root")),Oe()}},[[184,1,2]]]);
//# sourceMappingURL=main.133abbfc.chunk.js.map