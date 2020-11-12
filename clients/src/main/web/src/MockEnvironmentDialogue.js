import React from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Avatar from '@material-ui/core/Avatar';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import ListItemText from '@material-ui/core/ListItemText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Dialog from '@material-ui/core/Dialog';
import PersonIcon from '@material-ui/icons/Person';
import AddIcon from '@material-ui/icons/Add';
import Typography from '@material-ui/core/Typography';
import { blue } from '@material-ui/core/colors';
import {getNodes} from './remoteclient'
import ImageIcon from '@material-ui/icons/Image';
import AccountBalance from '@material-ui/icons/AccountBalance';
import AccountTreeIcon from '@material-ui/icons/AccountTree';
import EmojiEvents from '@material-ui/icons/EmojiEvents';
import ScheduleIcon from '@material-ui/icons/Schedule';

import WorkIcon from '@material-ui/icons/Work';
import BeachAccessIcon from '@material-ui/icons/BeachAccess';

const useStyles = makeStyles({
  avatar: {
    backgroundColor: blue[100],
    color: blue[600],
  },
});

function MockEnvironmentDialogue(props) {
  const classes = useStyles();
  const { onClose, selectedValue, open } = props;
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
  const handleClose = () => {
    onClose(selectedValue);
  };

  const handleListItemClick = (value) => {
    onClose(value);
  };

  return (
    <Dialog onClose={handleClose} aria-labelledby="simple-dialog-title" open={open}>
      <DialogTitle id="simple-dialog-title">Change mock node</DialogTitle>
      <List>
        {nodes && nodes.map((node) => (
          <ListItem button onClick={() => handleListItemClick(node)} key={node.name}>
            <ListItemAvatar>
                    <Avatar>
                        {node.name!='Notary' && <AccountTreeIcon />}
                        {node.name=='Notary' && <AccountBalance />}
                    </Avatar>
            </ListItemAvatar>
            <ListItemText primary={node.name} />
          </ListItem>
        ))}
      </List>
    </Dialog>
  );
}

MockEnvironmentDialogue.propTypes = {
  onClose: PropTypes.func.isRequired,
  open: PropTypes.bool.isRequired,
  selectedValue: PropTypes.string.isRequired,
};
export default MockEnvironmentDialogue;