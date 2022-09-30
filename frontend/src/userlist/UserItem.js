import React from 'react'
import { Link } from 'react-router-dom' 
import './UserList.css'

function UserItem(props) {

    const detailsLink = `/userdetails/${props.item.userID}` ;

    return (
        <tr>
            <td>
                <Link to = {detailsLink} className='ID-links'>
                    {props.item.userID}
                </Link>
            </td>
            <td>{props.item.fullName}</td>
            <td>{props.item.email}</td>
            <td>{props.item.title}</td>
        </tr> 
    )
}

export default UserItem