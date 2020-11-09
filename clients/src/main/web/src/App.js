import React from 'react';
import logo from './logo.svg';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import './App.css';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import Fab from '@material-ui/core/Fab';
import AddIcon from '@material-ui/icons/Add';
import EditIcon from '@material-ui/icons/Edit';
import FavoriteIcon from '@material-ui/icons/Favorite';
import NavigationIcon from '@material-ui/icons/Navigation';
import PlayArrow from '@material-ui/icons/PlayArrow';
import CancelIcon from '@material-ui/icons/Cancel';
import SwipeableViews from 'react-swipeable-views';
import Zoom from '@material-ui/core/Zoom';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import { green } from '@material-ui/core/colors';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import UpIcon from '@material-ui/icons/KeyboardArrowUp';
import TransactionList from './TransactionList'
import NodeExplorer from './NodeExplorer'
import NodeDetails from './NodeDetails'
import GameBoard from './GameBoard';
import {sendChallenge} from './remoteclient'
import TransactionDetails from './TransactionDetails';
/*
*/
function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <Typography
      component="div"
      role="tabpanel"
      hidden={value !== index}
      id={`action-tabpanel-${index}`}
      aria-labelledby={`action-tab-${index}`}
      {...other}
    >
      {value === index && <Box p={3}>{children}</Box>}
    </Typography>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.any.isRequired,
  value: PropTypes.any.isRequired,
};
const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: theme.palette.background.paper,
    width: 500,
    position: 'relative',
    minHeight: 200,
  },
  fab: {
    position: 'absolute',
    bottom: theme.spacing(2),
    right: theme.spacing(2),
  },
  fabGreen: {
    color: theme.palette.common.white,
    backgroundColor: green[500],
    '&:hover': {
      backgroundColor: green[600],
    },
  },
}));
function a11yProps(index) {
  return {
    id: `action-tab-${index}`,
    'aria-controls': `action-tabpanel-${index}`,
  };
}

function App() {
  const [value, setValue] = React.useState(0);
  const [gameboardReady, setGameboardReady] = React.useState(false);
  const [gameboard, setGameboard] = React.useState({});
  const [nodeDetails, setNodeDetails] = React.useState({});
  const [transactionDetails, setTransactionDetails] = React.useState({});
  const startGame = () =>{
    setValue(3);
  }
  const issueChallenge = () => {
    sendChallenge(gameboard, function(reply) {
      console.log("challenge issued",reply);
      setValue(0)
    })
  }
  const cancelGame = () =>{
    setValue(0);
  }
  const handleChange = (event, newValue) => {
    setValue(newValue);
  };
  const showNodeDetails = node => {
    setNodeDetails(node);
    setValue(2);
  }
  const showNodeTransactionDetails = transaction => {
    setTransactionDetails(transaction);
    setValue(2);
  }

  const handleChangeIndex = (index) => {
    setValue(index);
  };
  const changeGameboard = (game) => {
    setGameboardReady(game.ready);
    setGameboard(game);
  }
  React.useEffect(() => {
    console.log("value", value)
      if (value!=2) {
        setNodeDetails(null);
        setTransactionDetails(null)
      }
  }, [value]);
  const classes = useStyles();
  const theme = useTheme();
  const transitionDuration = {
    enter: theme.transitions.duration.enteringScreen,
    exit: theme.transitions.duration.leavingScreen,
  };

const fabs = [
  {
    color: 'primary',
    className: classes.fab,
    icon: <AddIcon />,
    label: 'Add',
    action: startGame,
    color2: 'secondary',
    display2: 'none'
  },
  {
    color: 'primary',
    className: classes.fab,
    icon: <AddIcon />,
    label: 'Add',
    action: startGame,
    color2: 'secondary',
    display2: 'none'
  },
  {
    color: 'inherit',
    className: clsx(classes.fab, classes.fabGreen),
    icon: <UpIcon />,
    label: 'Expand',
    display: 'none',
    display2: 'none'
  },
  {
    color: 'primary',
    className: clsx(classes.fab, classes.fabGreen),
    disabled: !gameboardReady,
    icon: <PlayArrow />,
    icon2: <CancelIcon />,
    label: 'Edit',
    action: issueChallenge,
    className2: clsx(classes.fab),
    color2: 'secondary',
    action2: cancelGame
  }
];
  return (
    <div className="App">
     <AppBar position="static" color="default">
        <Tabs
          value={value}
          onChange={handleChange}
          indicatorColor="primary"
          textColor="primary"
          variant="fullWidth"
          aria-label="action tabs example"
        >
          <Tab label="Transactions" {...a11yProps(0)} />
          <Tab label="Nodes" {...a11yProps(1)} />
          <Tab label={nodeDetails ? nodeDetails.name : transactionDetails ? transactionDetails.challengerChoice : ""} {...a11yProps(2)} />
        </Tabs>
      </AppBar>
      <SwipeableViews
        axis={theme.direction === 'rtl' ? 'x-reverse' : 'x'}
        index={value}
        onChangeIndex={handleChangeIndex}
      >
        <TabPanel value={value} index={0} dir={theme.direction} >
          <TransactionList onTransactionSelected={showNodeTransactionDetails}/>
        </TabPanel>
        <TabPanel value={value} index={1} dir={theme.direction}>
        <NodeExplorer onNodeSelected={showNodeDetails}/>
        </TabPanel>
        <TabPanel value={value} index={2} dir={theme.direction}>
          {nodeDetails &&
            <NodeDetails node={nodeDetails}/>
          }
          {transactionDetails &&
            <TransactionDetails transaction={transactionDetails}/>
          }
          
        </TabPanel>
        <TabPanel value={value} index={3} dir={theme.direction}>
          <GameBoard onChange={changeGameboard}/>
        </TabPanel>
      </SwipeableViews>
      {fabs.map((fab, index) => (
        <>
        <Zoom
          key={fab.color}
          in={value === index}
          timeout={transitionDuration}
          style={{
            transitionDelay: `${value === index ? transitionDuration.exit : 0}ms`,
          }}
          unmountOnExit
        >
          <Fab aria-label={fab.label} className={fab.className} color={fab.color}
            style={{display:fab.display}}
            disabled={fab.disabled}
            onClick={fab.action}>
            {fab.icon}
          </Fab>
        </Zoom>

        <Zoom
          key={fab.color}
          in={value === index}
          timeout={transitionDuration}
          style={{
            transitionDelay: `${value === index ? transitionDuration.exit : 0}ms`,
          }}
          unmountOnExit
        >
          <Fab aria-label={fab.label} className={fab.className2} color={fab.color2}
            style={{display:fab.display2, right:'80px'}}
            onClick={fab.action2}>
            {fab.icon2}
          </Fab>
        </Zoom>

        </>
      ))}
    </div>
  );
}

export default App;
