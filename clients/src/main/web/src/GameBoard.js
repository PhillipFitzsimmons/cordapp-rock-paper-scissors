import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import {getNodes} from './remoteclient'

const useStyles = makeStyles((theme) => ({
    formControl: {
        margin: theme.spacing(1),
        minWidth: '90%',
    },
    selectEmpty: {
        marginTop: theme.spacing(2),
    },
}));

export default function GameBoard(props) {
    const { onChange, onError, ...other } = props;
    const classes = useStyles();
    const [escrow, setEscrow] = React.useState('');
    const [counterParty, setCounterParty] = React.useState('');
    const [choice, setChoice] = React.useState('');
    const [nodes, setNodes] = React.useState('');
    const [error, setError] = React.useState('');
    const handleChoiceChange = (event) => {
        setChoice(event.target.value);
    };
    const handleEscrowChange = (event) => {
        setEscrow(event.target.value);
    };
    const handleCounterpartyChange = (event) => {
        setCounterParty(event.target.value);
    };

    React.useEffect(() => {
        getNodes((error, nodeList)=>{
            console.log("Gameboard", nodeList);
            if (error) {
                onError(error);
                return;
            }
            for (var i=0;i<nodeList.length;i++) {
                nodeList[i].name=nodeList[i].identities[0].name.organisation;
            };
            setNodes(nodeList);
        });
    }, []);
    React.useEffect(() => {
        let ready=false;
        if (choice && counterParty && escrow) {
            if (counterParty===escrow) {
             setError("Escrow and counterparty cannot be the same party");
            } else {
                let currentNode=nodes.find(node=>node.isCurrentNode);
                if (currentNode.name===counterParty) {
                    setError("Counterparty and current node cannot be the same node");
                } else if (currentNode.name===escrow) {
                    setError("Escrow and current node cannot be the same node");
                } else if (counterParty==='Notary' || escrow==='Notary') {
                    setError("Notary cannot be a signing party");
                } else {
                    setError(false);
                    ready=true;
                }
            }
        }
        if (onChange) {
            onChange({ready, escrow, counterParty, choice});
        }
      }, [counterParty, escrow, choice]);
    return (
        <div>
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
            <FormControl className={classes.formControl} error={error}>
                <InputLabel id="demo-simple-select-label">Escrow</InputLabel>
                <Select
                    labelId="demo-simple-select-label"
                    id="demo-simple-select"
                    value={escrow}
                    onChange={handleEscrowChange}
                >
                    {
                        nodes && nodes.map(node=>{
                            return (
                            <MenuItem value={node.name}>{node.name}</MenuItem>
                            )
                        })
                    }
                </Select>
            </FormControl>
            <FormControl className={classes.formControl} error={error}>
                <InputLabel id="demo-simple-select-label">Counter Party</InputLabel>
                <Select
                    labelId="demo-simple-select-label"
                    id="demo-simple-select"
                    value={counterParty}
                    onChange={handleCounterpartyChange}
                >
                    {
                        nodes && nodes.map(node=>{
                            return (
                            <MenuItem value={node.name}>{node.name}</MenuItem>
                            )
                        })
                    }
                </Select>
            </FormControl>
                <FormHelperText style={{display:error ? '' : 'none'}}>{error}</FormHelperText>
        </div>
    )
}