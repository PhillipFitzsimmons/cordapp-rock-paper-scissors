import JSONTree from 'react-json-tree'
import ReactJson from 'react-json-view'

function NodeDetails(props) {
    const { node, ...other } = props;
    /*return (
        <JSONTree data={node} />
    )*/
    return (
        <div style={{textAlign:'left'}}>
        <ReactJson src={node} />
        </div>
    )
}

export default NodeDetails;