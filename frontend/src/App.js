import React, {useState, useEffect} from 'react';
import Navbar from './components/Navbar';
import UserList from './userlist/UserList'
import UserDetails from './userdetails/UserDetails'
import CreateUser from './createuser/CreateUser';
import Login from './login/Login';
import { BrowserRouter as Router , Routes, Route } from 'react-router-dom';
import './App.css';
import  { Navigate } from 'react-router-dom'
import Home from './home/Home';


function getToken(){
  const token = localStorage.getItem('token');
  return token ;
}


function App() {

  console.log("token: " + getToken());

  const token = getToken();

  console.log(window.location.pathname);

  if(!(token) && (window.location.pathname != '/login')) {
    console.log("Navigate");
    console.log(window.location.pathname);

    window.location.href = '/login';
    
  }

  return (
      <Router>
        {token && <header className='App-header'>
          <div>
            <Navbar/>
          </div>
       </header>}
        <Routes>
          <Route path = "/" exact ></Route>
          <Route path = "/login" element={<Login />}></Route>
          <Route path = "/home" element={<Home />}></Route>
          <Route path = "/userlist" element={<UserList/>}></Route>
          <Route path = "/userdetails/:userID" element={<UserDetails/>}></Route>
          <Route path = "/create" element={<CreateUser/>}></Route>
        </Routes>
      </Router>
  );
}

<script>

  
</script>

export default App;
