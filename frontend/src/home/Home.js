import React, {useState, useEffect} from 'react';
import Navbar from '../components/Navbar';
import UserList from '../userlist/UserList'
import UserDetails from '../userdetails/UserDetails'
import CreateUser from '../createuser/CreateUser';
import { BrowserRouter as Router , Routes, Route } from 'react-router-dom';
import '../App.css';

function Home() {
  return (
    <div>
      <h1>Home</h1>
    </div>
  );
}

export default Home