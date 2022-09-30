import { Component } from '@angular/core';
import { GetApiService } from './get-api.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent {
  title = 'frontendAngular';
  userList: [];

  constructor(private api: GetApiService){

  }

  ngOnInit(){
    this.api.apiGetUserList().subscribe((data) => {
      console.warn("get api data", data);
      this.userList = data['data'];
      console.warn(this.userList);
    })
  }
}
