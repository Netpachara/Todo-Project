import { Component, OnInit } from '@angular/core';
import { GetApiService } from 'src/app/get-api.service';

@Component({
  selector: 'app-userlist',
  templateUrl: './userlist.component.html',
  styleUrls: ['./userlist.component.scss']
})
export class UserlistComponent implements OnInit {

  userList : [];

  constructor(private api: GetApiService) { }

  ngOnInit() {
    this.api.apiGetUserList().subscribe((data) => {
      console.warn("get api data", data);
      this.userList = data['data'];
      console.warn(this.userList);
    })
  }

  

}
