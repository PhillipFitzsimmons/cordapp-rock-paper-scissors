import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import Avatar from '@material-ui/core/Avatar';

import ImageIcon from '@material-ui/icons/Image';
import AccountBalance from '@material-ui/icons/AccountBalance';
import EmojiEvents from '@material-ui/icons/EmojiEvents';
import ScheduleIcon from '@material-ui/icons/Schedule';

import WorkIcon from '@material-ui/icons/Work';
import BeachAccessIcon from '@material-ui/icons/BeachAccess';
import {getTransactions} from './remoteclient'


const useStyles = makeStyles((theme) => ({
    root: {
      width: '100%',
      maxWidth: 360,
      backgroundColor: theme.palette.background.paper,
    },
  }));

function TransactionList(props) {
    const { onTransactionSelected, ...other } = props;
    const classes = useStyles();
    const [txList, setTransactionList] = React.useState([]);
    React.useEffect(() => {
        getTransactions(function(transactions) {
            console.log("transactions", transactions);
            //reduce to the latest transaction with the same linear ID
            let t=transactions.sort((a,b)=>b.recordedTime-a.recordedTime)
            t=t.filter(element=>element.recordedTime==transactions.find(item=>item.id==element.id).recordedTime);

            console.log("transactions after filtering", t);
            setTransactionList(t);
        })
      }, []);
      
    return (
        <List className={classes.root}>
            {txList && txList.map((item,i)=>{
                return(
                <ListItem onClick={()=>onTransactionSelected(item)} style={{cursor:'pointer'}}>
                    <ListItemAvatar>
                    <Avatar>
                        {item.class=='com.axa.RockPaperScissorsIssuedState' && <EmojiEvents />}
                        {item.status=='com.axa.RockPaperScissorsChallengeState' && <ScheduleIcon />}
                        {item.status=='com.axa.RockPaperScissorsAcceptedState' && <ScheduleIcon />}
                        {item.status=='com.axa.RockPaperScissorsSettledState' && <AccountBalance />}
                    </Avatar>
                    </ListItemAvatar>
                    <ListItemText primary={item.id} secondary={/*new Date(1*item.recordedTime).toISOString()*/item.class} />
              </ListItem>
                )
            })
            }
        </List>
    )
}
export default TransactionList;