import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import Avatar from '@material-ui/core/Avatar';

import ImageIcon from '@material-ui/icons/Image';
import AccountBalance from '@material-ui/icons/AccountBalance';
import AccountTreeIcon from '@material-ui/icons/AccountTree';
import EmojiEvents from '@material-ui/icons/EmojiEvents';
import ScheduleIcon from '@material-ui/icons/Schedule';

import WorkIcon from '@material-ui/icons/Work';
import BeachAccessIcon from '@material-ui/icons/BeachAccess';
import {getNodes} from './remoteclient'


const useStyles = makeStyles((theme) => ({
    root: {
      width: '100%',
      maxWidth: 360,
      backgroundColor: theme.palette.background.paper,
    },
  }));

function NodeExplorer(props) {
    const { onNodeSelected, ...other } = props;
    const classes = useStyles();
    const [nodes, setNodes] = React.useState('');
    React.useEffect(() => {
        getNodes(nodeList=>{
            console.log("NodeExplorer", nodeList);
            for (var i=0;i<nodeList.length;i++) {
                nodeList[i].name=nodeList[i].identities[0].name.organisation;
                nodeList[i].address=nodeList[i].addresses[0];
            };
            setNodes(nodeList);
        });
    }, []);
    return (
        <List className={classes.root}>
            {nodes && nodes.map((node,i)=>{
                return(
                <ListItem onClick={()=>onNodeSelected(node)} style={{cursor:'pointer'}}>
                    <ListItemAvatar>
                    <Avatar>
                        {node.name!='Notary' && <AccountTreeIcon />}
                        {node.name=='Notary' && <AccountBalance />}
                    </Avatar>
                    </ListItemAvatar>
                    <ListItemText primary={node.name} secondary={node.address} />
              </ListItem>
                )
            })
            }
        </List>
    )
}
export default NodeExplorer;