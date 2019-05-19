/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Sistema;

import OhmOMatic.ProtoBuffer.HomeOuterClass.parametriCasaReq;
import OhmOMatic.ProtoBuffer.HomeOuterClass.parametriCasaRes;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.net.URI;

public final class Admin
{

    private Client client;
    private WebTarget webTarget;


    public Admin(URI indirizzo)
    {
        client = ClientBuilder.newClient();
        webTarget = client.target(indirizzo + "/OOM");
    }

    //region Funzioni
    public void iscriviCasa(int ID)
    {
        try
        {
            var wt_iscriviCasa = webTarget
                    .path("iscriviCasa");

            final var par = parametriCasaReq
                    .newBuilder()
                    .setID(5)
                    .build();

            final var res = wt_iscriviCasa
                    .request()
                    .put(Entity.entity(par, "application/x-protobuf"), parametriCasaRes.class);

            if (res.getOk())
                System.out.println("OK!");
            else
                System.out.println(res.getErrore());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void disiscriviCasa(int ID)
    {
        try
        {
            var wt_disiscriviCasa = webTarget
                    .path("disiscriviCasa");

            final var par = parametriCasaReq
                    .newBuilder()
                    .setID(5)
                    .build();

            final var res = wt_disiscriviCasa
                    .request()
                    .put(Entity.entity(par, "application/x-protobuf"), parametriCasaRes.class);

            if (res.getOk())
                System.out.println("OK!");
            else
                System.out.println(res.getErrore());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //endregion


}