import React , {useState, useEffect} from 'react';
import axios from 'axios';
import './UserDetails.css';
import { useParams } from 'react-router-dom';

const UserDetails = () => {

  const [userDetails, setUserDetails] = useState();

  const [errors, setError] = useState();

  const {userID} = useParams();

    const fetchUserDetails = () => {
      axios.get(`http://localhost:8080/user/api/user/${userID}/details`).then(res => {
        console.log(res.data);
        setUserDetails(res.data);
      }).catch(error => {
          console.log(error.response.data.errors);
          setError(error.response.data.errors);
      });
    };  

  useEffect(() => {
    fetchUserDetails();
  },  []);

  return (
  <div className='UD-bg'>{
    userDetails && (
    <div>
      <h1>UserID: {userDetails.userID}</h1>
      <p>FullName: {userDetails.fullName}</p>
      <p>Email: {userDetails.email}</p>
      {userDetails.responseRoleList.map(role => (
        <p key = {role.roleID}>Role: {role.roleID}</p>
      ))}
      <p>UserIDParams: {userID}</p>
    </div>)
  }
  {
    errors && (
      <div>
        <p>Errors: {errors}</p>
      </div>
    )
  }
    
  </div>)
};



export default UserDetails