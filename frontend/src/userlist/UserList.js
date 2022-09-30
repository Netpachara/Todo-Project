import React, { useState, useEffect } from 'react'
import axios from 'axios';
import './UserList.css'
import UserItem from './UserItem';
import Navbar from '../components/Navbar';


const UserList = () => {

  const[userlist, setUserList] = useState([]);

  const [click, setClick] = useState(false);

  const closeMenu = () => setClick(false);

  const [search, setSearch] = useState({
    search : "",
    role: ""
  }) ;

  const fetchUserList = () => {
    console.log(search.role);
    axios.get(`http://localhost:8080/user/api/user/list?search=${search.search}&roleID=${search.role}&page=1&pageSize=10`).then(res => {
      console.log(res.data.data);
      setUserList(res.data.data);
    });
  };

  const handle = (e) => {
    console.log(e.target.name);
    console.log(e.target.value);

    setSearch((Search) => {
      return {
          ...Search,
          [e.target.name]:e.target.value}
    })
  }

  const submit = (e) => {
    e.preventDefault();
    fetchUserList();
  }
  
  useEffect(() => {
    fetchUserList();
  },  []);

  return (
    <div className = "UList-bg">
        <h1>UserList</h1>
      <form onSubmit={submit}>
        <input onChange={handle} name = "search" type = "text" className='Input_field' placeholder = 'Search...'/>
        <select name = "role" onChange={handle}>
          <option value = "1">admin</option>
          <option value = "2">member</option>
          <option value = "1,2">admin,member</option>
        </select>
        <button type ="submit">Search</button>
      </form>
      <table border = "1" cellPadding={10} className='table'>
        <thead>
          <tr>
              <th>UserID</th>
              <th>FullName</th>
              <th>Email</th>
              <th>Title</th>
          </tr>
        </thead>
        <tbody>
          {userlist.map((item, index) => {
            return (
                <UserItem item = {item} key = {index}/>
            )
          })}
        </tbody>
      </table>


    </div>
  )
}



export default UserList