import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './pages/home/Home';
import Connect from './pages/connect/Connect';
import TempConnect from './pages/connect/TempConnect';
import AdminResource from './pages/admin/Resource';

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route exact path={'/'} element={<Home/>} />
        <Route exact path={'/connect/'} element={<Connect/>} />
        <Route exact path={'/temp'} element={<TempConnect/>} />
        <Route exact path={'/admin'} element={<AdminResource/>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
