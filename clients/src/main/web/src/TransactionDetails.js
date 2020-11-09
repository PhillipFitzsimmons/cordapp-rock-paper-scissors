import JSONTree from 'react-json-tree'
import ReactJson from 'react-json-view'

function TransactionDetails(props) {
    const { transaction, ...other } = props;
    /*return (
        <JSONTree data={node} />
    )*/
    return (
        <div style={{textAlign:'left'}}>
        <ReactJson src={transaction} />
        </div>
    )
}

export default TransactionDetails;