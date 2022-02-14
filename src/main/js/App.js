import './App.css';
import * as React from 'react';
import { useEffect, useState } from 'react'
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';


function App() {

    const [titles, setTitles] = useState([]);
    const [url, setUrl] = useState("");

    useEffect(() => {
        fetch('titles')
            .then(response => response.json())
            .then(data => setTitles(data));
    }, [])

    function add() {
        fetch('titles', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                url: url,
            })

        })
            .then(response => response.json())
            .then(data => setTitles([data].concat(titles)));
    }

    return (
        <div className="App">
            <h1>Title and favicon</h1>
            <div className="flex">
                <TextField label="Enter a URL" fullWidth={true}
                    onChange={event => setUrl(event.target.value)} />
                <Button size="large" variant="contained"
                    onClick={add}
                >Add</Button>
            </div>
            <TitleTable titles={titles} />
        </div>
    );
}

export default App;

function TitleTable({ titles }) {
    return (
        <TableContainer component={Paper}>
            <Table aria-label="simple table">
                <TableHead>
                    <TableRow>
                        <TableCell>URL</TableCell>
                        <TableCell>Title</TableCell>
                        <TableCell>Favicon</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {titles.map(Row)}
                </TableBody>
            </Table>
        </TableContainer>
    );
}

function Row({ hasFavicon, id, title, url }) {
    return <TableRow
        key={id}
    >
        <TableCell><a href={url} target="_blank">{url}</a></TableCell>
        <TableCell>{title || "No title found"}</TableCell>
        <TableCell>
            {hasFavicon ?
                <img src={`titles/${id}/favicon.ico`} className="faviconImage" /> :
                "No favicon found"}
        </TableCell>
    </TableRow>
}