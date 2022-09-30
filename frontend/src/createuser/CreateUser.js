import axios from 'axios';
import React, { useState } from 'react'
import './CreateUser.css' ;
import Navbar from '../components/Navbar';

function CreateUser() {
  const url = "http://localhost:8080/api/user/create" ;
  const [data, setData] = useState({
      fullName: "",
      email: "",
      password: "",    
  });

  function handle(e){
        // const newdata={...data}
        // newdata[e.target.id] = e.target.value
        // setData(newdata)

        // console.log(newdata)

        setData((preData) => {
            return {
                ...preData,
                [e.target.id]:e.target.value}
        })

  }

  function submit(e){
    e.preventDefault();
    axios.post(url,{
        fullName: data.fullName,
        email: data.email,
        password: data.password
    })
    .then(res => {
        console.log(res.data);
    })
    
  }

  return (
    <div>
        <div className='Input-bg'>
          <h1>CreateUser</h1>
          <form onSubmit={(e)=> submit(e)}>
              <input onChange = {(e)=>handle(e)} id="fullName" value = {data.fullName} className='Input_field' type="text" placeholder='FullName'/><br/>
              <input onChange = {(e)=>handle(e)} id="email" value = {data.email} className ='Input_field' type="text" placeholder='Email'/><br/>
              <input onChange = {(e)=>handle(e)} id="password" value = {data.password} className='Input_field' type="text" placeholder='Password'/><br/>
              <button>Submit</button>
          </form>
        </div>
    </div>
  )
}

export default CreateUser