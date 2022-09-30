import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { MyChartComponent } from './components/my-chart/my-chart.component';
import { UserdetailsComponent } from './components/userdetails/userdetails.component';
import { UserlistComponent } from './components/userlist/userlist.component';


const routes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: 'userlist', component: UserlistComponent},
  {path: 'my-chart', component: MyChartComponent},
  {path: 'userdetails/:id', component: UserdetailsComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule { }
export const routingComponents = [HomeComponent, UserlistComponent, MyChartComponent];
