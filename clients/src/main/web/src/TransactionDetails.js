import React from 'react';
import JSONTree from 'react-json-tree'
import ReactJson from 'react-json-view'
import {getTransactionDetails, sendAcceptChallenge} from './remoteclient'
import { makeStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import StepContent from '@material-ui/core/StepContent';
import Button from '@material-ui/core/Button';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import AccountBalance from '@material-ui/icons/AccountBalance';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';

const useStyles = makeStyles((theme) => ({
    root: {
      width: '100%',
    },
    button: {
      marginTop: theme.spacing(1),
      marginRight: theme.spacing(1),
    },
    actionsContainer: {
      marginBottom: theme.spacing(2),
    },
    resetContainer: {
      padding: theme.spacing(3),
    },
    formControl: {
        margin: theme.spacing(1),
        minWidth: '90%',
    },
    selectEmpty: {
        marginTop: theme.spacing(2),
    },
  }));
  
  function getSteps(transactionDetails) {
    return ['Issued State', 'Challenge State', 'Accepted State', 'Settled State'];
  }

function TransactionDetails(props) {
    const { transaction, ...other } = props;
    const [transactionDetails, setTransactionDetails] = React.useState([]);
    const [choice, setChoice] = React.useState('');
    React.useEffect(() => {
        getTransactionDetails(transaction, function(transactionDetails) {
            console.log(transactionDetails);
            setTransactionDetails(transactionDetails);
        })
      }, []);
      const classes = useStyles();
  const [activeStep, setActiveStep] = React.useState(0);
  const steps = getSteps(transactionDetails);

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleReset = () => {
    setActiveStep(0);
  };
  const handleChoiceChange = (event) => {
      setChoice(event.target.value);
  };
  const acceptChallenge = () => {
    transactionDetails.ChallengeState.choice=choice;
    sendAcceptChallenge(transactionDetails.ChallengeState, ()=>{
      getTransactionDetails(transaction, function(transactionDetails) {
          setTransactionDetails(transactionDetails);
      })
    })
  }

  function getStepContent(step, transactionDetails) {
    let display=<></>
  switch (step) {
    case 0:
      display=<>There is no Issued State</>;
      if (transactionDetails) {
          if (transactionDetails.ChallengeState && transactionDetails.ChallengeState.challenged.name == transactionDetails.currentNode.name) {
              display=<>You are the counter party and not a signatory to this state.
              Your vault only contains states of which you are a signatory</>;
          } else {
              display = <ReactJson src={transactionDetails.IssuedState} collapsed/>
          }
      }
      return  <div>
          {display}
          </div>;
    case 1:
      display=<>There is no Challenge State</>;
      if (transactionDetails) {
          if (transactionDetails.IssuedState && transactionDetails.IssuedState.challenger.name == transactionDetails.currentNode.name) {
              display=<>You are the challenger and not a signatory to this state.
              Your vault only contains states of which you are a signatory</>;
          } else  if (transactionDetails.ChallengeState) {
              display = <ReactJson src={transactionDetails.ChallengeState} collapsed/>
          }
      }
      return  <div>
          {display}
          </div>;
    case 2:
      display=<>There is no Accepted State - the challenge hasn't been accepted.</>;
      if (transactionDetails) {
          if (transactionDetails.SettledState && 
              transactionDetails.SettledState.challenger.name == transactionDetails.currentNode.name) {
              display=<>You are the challenger and not a signatory to this state.
              Your vault only contains states of which you are a signatory</>;
          } else if (transactionDetails.SettledState && 
            transactionDetails.SettledState.escrow.name == transactionDetails.currentNode.name) {
            display=<>You are the escrow and should see this. This is a bug.</>;
            //display = <ReactJson src={transactionDetails.AcceptedState} collapsed/>
          } else if (transactionDetails.AcceptedState) {
              display = <ReactJson src={transactionDetails.AcceptedState} collapsed/>
          } else if (transactionDetails.ChallengeState && transactionDetails.ChallengeState.challenged.name == transactionDetails.currentNode.name) {
            display = <>
              <FormControl className={classes.formControl}>
                <InputLabel id="demo-simple-select-label">Choice</InputLabel>
                <Select
                    labelId="demo-simple-select-label"
                    id="demo-simple-select"
                    value={choice}
                    onChange={handleChoiceChange}
                >
                    <MenuItem value={'rock'}>Rock</MenuItem>
                    <MenuItem value={'paper'}>Paper</MenuItem>
                    <MenuItem value={'scissors'}>Scissors</MenuItem>
                </Select>
            </FormControl>
                <Button 
                  variant="contained"
                  color="primary"
                  className={classes.button}
                  onClick={acceptChallenge}
                  disabled={choice==''}
                >
                  Accept
                </Button>
                </>
        }
      }
      return  <div>
          {display}
          </div>;
    case 3:
      display=<>There is no Settled State</>;
      if (transactionDetails && transactionDetails.SettledState) {
              display = <ReactJson src={transactionDetails.SettledState} collapsed/>
      }
      return  <div>
          {display}
          </div>;
    default:
      return 'Unknown step';
  }
}
    /*return (
        <div style={{textAlign:'left'}}>
        <ReactJson src={transaction} />
        </div>
    )*/
    return (
        <div className={classes.root} style={{textAlign:'left'}}>
          <Stepper activeStep={activeStep} orientation="vertical">
            {steps.map((label, index) => (
              <Step key={label}>
                <StepLabel onClick={()=>setActiveStep(index)}>{label}</StepLabel>
                <StepContent>
                  <Typography>{getStepContent(index, transactionDetails)}</Typography>
                  {/*
                  <div className={classes.actionsContainer}>
                    <div>
                      <Button
                        disabled={activeStep === 0}
                        onClick={handleBack}
                        className={classes.button}
                      >
                        Back
                      </Button>
                      <Button
                        variant="contained"
                        color="primary"
                        onClick={handleNext}
                        className={classes.button}
                      >
                        {activeStep === steps.length - 1 ? 'Finish' : 'Next'}
                      </Button>
                    </div>
                  </div>
                  */}
                </StepContent>
              </Step>
            ))}
          </Stepper>
          {/*activeStep === steps.length && (
            <Paper square elevation={0} className={classes.resetContainer}>
              <Typography>All steps completed - you&apos;re finished</Typography>
              <Button onClick={handleReset} className={classes.button}>
                Reset
              </Button>
            </Paper>
          )*/}
        </div>
      );
}
export default TransactionDetails;