import { Container, Typography, Button } from "@mui/material";
import { Link } from "react-router-dom";
export default function NotFound(){return <Container sx={{py:10,textAlign:"center"}}><Typography variant="h2">404</Typography><Typography>Page not found.</Typography><Button component={Link} to="/" variant="contained" sx={{mt:2}}>Home</Button></Container>}